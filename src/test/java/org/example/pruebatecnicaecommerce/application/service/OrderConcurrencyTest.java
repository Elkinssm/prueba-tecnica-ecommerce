package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.ItemRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.example.pruebatecnicaecommerce.shared.error.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Concurrency Tests - Order Processing")
class OrderConcurrencyTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderService createOrderService;

    @InjectMocks
    private PayOrderService payOrderService;

    private final UUID PRODUCT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID CUSTOMER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        // Arrange - Reset counters and mocks for each test
        reset(orderRepository, inventoryRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should handle concurrent order creation successfully")
    void shouldHandleConcurrentOrderCreationSuccessfully() throws InterruptedException, ExecutionException, TimeoutException {
        // Arrange
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            // Simulate some processing time
            Thread.sleep(10);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
            CUSTOMER_ID.toString(),
            List.of(new ItemRequest(PRODUCT_ID.toString(), 1, new BigDecimal("29.99")))
        );

        // Act
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    OrderResponse response = createOrderService.execute(request);
                    if (response != null) {
                        successfulOrders.incrementAndGet();
                    }
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                }
            }, executor));
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertThat(successfulOrders.get()).isEqualTo(numberOfThreads);
        assertThat(failedOrders.get()).isEqualTo(0);
        verify(orderRepository, times(numberOfThreads)).save(any(Order.class));
        verify(eventPublisher, times(numberOfThreads)).publish(any());
    }

    @Test
    @DisplayName("Should handle concurrent inventory reservation correctly")
    void shouldHandleConcurrentInventoryReservationCorrectly() throws InterruptedException, ExecutionException, TimeoutException {
        // Arrange
        int numberOfThreads = 5;
        int initialStock = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successfulPayments = new AtomicInteger(0);
        AtomicInteger failedPayments = new AtomicInteger(0);

        Inventory inventory = Inventory.create(PRODUCT_ID, initialStock);
        Order order = Order.create(CUSTOMER_ID);
        order.addItem(new org.example.pruebatecnicaecommerce.domain.model.order.OrderItem(
            PRODUCT_ID, 3, new BigDecimal("29.99"))); // Each order needs 3 items

        when(orderRepository.findByPublicId(anyString())).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            final String orderId = "ORD-" + i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    OrderResponse response = payOrderService.execute(orderId);
                    if (response != null) {
                        successfulPayments.incrementAndGet();
                    }
                } catch (InsufficientStockException e) {
                    failedPayments.incrementAndGet();
                } catch (Exception e) {
                    failedPayments.incrementAndGet();
                }
            }, executor);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        // With 10 initial stock and 5 orders of 3 items each (15 total needed),
        // some orders should fail due to insufficient stock
        assertThat(successfulPayments.get() + failedPayments.get()).isEqualTo(numberOfThreads);
        assertThat(failedPayments.get()).isGreaterThan(0); // Some should fail due to stock limits
    }

    @Test
    @DisplayName("Should maintain data consistency under concurrent access")
    void shouldMaintainDataConsistencyUnderConcurrentAccess() throws InterruptedException, ExecutionException, TimeoutException {
        // Arrange
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger totalOperations = new AtomicInteger(0);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            totalOperations.incrementAndGet();
            return invocation.getArgument(0);
        });

        CreateOrderRequest request = new CreateOrderRequest(
            CUSTOMER_ID.toString(),
            List.of(new ItemRequest(PRODUCT_ID.toString(), 1, new BigDecimal("29.99")))
        );

        // Act
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    createOrderService.execute(request);
                } catch (Exception e) {
                    // Ignore exceptions for this consistency test
                }
            }, executor);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertThat(totalOperations.get()).isEqualTo(numberOfThreads);
        verify(orderRepository, times(numberOfThreads)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle high load without deadlocks")
    void shouldHandleHighLoadWithoutDeadlocks() throws InterruptedException, ExecutionException, TimeoutException {
        // Arrange
        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger completedOperations = new AtomicInteger(0);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            // Simulate database processing time
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return invocation.getArgument(0);
        });

        CreateOrderRequest request = new CreateOrderRequest(
            CUSTOMER_ID.toString(),
            List.of(new ItemRequest(PRODUCT_ID.toString(), 1, new BigDecimal("29.99")))
        );

        // Act
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    createOrderService.execute(request);
                    completedOperations.incrementAndGet();
                } catch (Exception e) {
                    // Log but don't fail the test
                    System.err.println("Operation failed: " + e.getMessage());
                }
            }, executor);
        }

        // Wait for all threads to complete (with timeout to prevent deadlocks)
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        executor.shutdown();
        long endTime = System.currentTimeMillis();

        // Assert
        assertThat(completedOperations.get()).isEqualTo(numberOfThreads);
        assertThat(endTime - startTime).isLessThan(30000); // Should complete within 30 seconds
        verify(orderRepository, times(numberOfThreads)).save(any(Order.class));
    }
}
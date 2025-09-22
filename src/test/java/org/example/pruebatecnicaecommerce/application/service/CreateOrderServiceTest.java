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

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderService Unit Tests")
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderService createOrderService;

    private CreateOrderRequest createOrderRequest;
    private Order savedOrder;
    private Inventory inventory1;
    private Inventory inventory2;

    @BeforeEach
    void setUp() {
        ItemRequest item1 = new ItemRequest("PROD-11111111", 2, new BigDecimal("29.99"));
        ItemRequest item2 = new ItemRequest("PROD-22222222", 1, new BigDecimal("15.50"));

        createOrderRequest = new CreateOrderRequest(List.of(item1, item2));

        savedOrder = Order.create(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        savedOrder.addItem(new org.example.pruebatecnicaecommerce.domain.model.order.OrderItem(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, new BigDecimal("29.99")));
        savedOrder.addItem(new org.example.pruebatecnicaecommerce.domain.model.order.OrderItem(
                UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"), 1, new BigDecimal("15.50")));

        // Mock inventory lookups
        inventory1 = Inventory.restore(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "PROD-11111111", 100, 0);
        inventory2 = Inventory.restore(
                UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
                "PROD-22222222", 50, 0);

        when(inventoryRepository.findByProductCode("PROD-11111111")).thenReturn(Optional.of(inventory1));
        when(inventoryRepository.findByProductCode("PROD-22222222")).thenReturn(Optional.of(inventory2));
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse result = createOrderService.execute(createOrderRequest, "123e4567-e89b-12d3-a456-426614174000");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(result.getStatus()).isEqualTo("CREATED");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(new BigDecimal("75.48"));
        assertThat(inventory1.getStock()).isEqualTo(98);
        assertThat(inventory2.getStock()).isEqualTo(49);

        verify(orderRepository).save(
                argThat(order -> order.getCustomerId().toString().equals("123e4567-e89b-12d3-a456-426614174000")));
        verify(eventPublisher, times(2)).publish(any()); // OrderCreated + OrderStatusChanged events
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should publish OrderCreatedEvent after saving")
    void shouldPublishOrderCreatedEventAfterSaving() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        createOrderService.execute(createOrderRequest, "123e4567-e89b-12d3-a456-426614174000");

        // Assert
        verify(eventPublisher, atLeastOnce()).publish(argThat(event -> event.getEventType().equals("OrderCreated")));
    }

    @Test
    @DisplayName("Should calculate total correctly")
    void shouldCalculateTotalCorrectly() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse result = createOrderService.execute(createOrderRequest, "123e4567-e89b-12d3-a456-426614174000");

        // Assert
        // 2 * 29.99 + 1 * 15.50 = 75.48
        assertThat(result.getTotal()).isEqualTo(new BigDecimal("75.48"));
    }

    @Test
    @DisplayName("Should save order with correct items")
    void shouldSaveOrderWithCorrectItems() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        createOrderService.execute(createOrderRequest, "123e4567-e89b-12d3-a456-426614174000");

        // Assert
        verify(orderRepository).save(argThat(order -> {
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2);
            assertThat(order.getItems().get(1).getQuantity()).isEqualTo(1);
            return true;
        }));
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should rollback when stock is insufficient")
    void shouldRollbackWhenStockIsInsufficient() {
        // Arrange
        Inventory insufficientInventory = Inventory.restore(
                UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
                "PROD-22222222", 0, 0);
        when(inventoryRepository.findByProductCode("PROD-22222222")).thenReturn(Optional.of(insufficientInventory));

        // Act & Assert
        assertThatThrownBy(() -> createOrderService.execute(createOrderRequest, "123e4567-e89b-12d3-a456-426614174000"))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publish(any());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }
}
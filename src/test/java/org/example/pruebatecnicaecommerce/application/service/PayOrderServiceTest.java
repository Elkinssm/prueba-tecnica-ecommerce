package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.example.pruebatecnicaecommerce.shared.error.InventoryNotFoundException;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayOrderService Unit Tests")
class PayOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PayOrderService payOrderService;

    private Order order;
    private Inventory inventory;
    private final String PUBLIC_ORDER_ID = "ORD-20250921-TEST";
    private final UUID PRODUCT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        order = Order.create(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        order.addItem(new OrderItem(PRODUCT_ID, 2, new BigDecimal("29.99")));
        
        inventory = Inventory.create(PRODUCT_ID, 100);
    }

    @Test
    @DisplayName("Should pay order successfully")
    void shouldPayOrderSuccessfully() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ORDER_ID)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = payOrderService.execute(PUBLIC_ORDER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PAID");
        
        verify(inventoryRepository).save(argThat(inv -> inv.getStock() == 98)); // 100 - 2 = 98
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> payOrderService.execute(PUBLIC_ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class);
        
        verify(inventoryRepository, never()).findByProductId(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when inventory not found")
    void shouldThrowExceptionWhenInventoryNotFound() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ORDER_ID)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> payOrderService.execute(PUBLIC_ORDER_ID))
                .isInstanceOf(InventoryNotFoundException.class);
        
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should reserve correct inventory quantity")
    void shouldReserveCorrectInventoryQuantity() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ORDER_ID)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        payOrderService.execute(PUBLIC_ORDER_ID);

        // Assert
        verify(inventoryRepository).save(argThat(inv -> {
            assertThat(inv.getStock()).isEqualTo(98); // 100 - 2 = 98
            return true;
        }));
    }

    @Test
    @DisplayName("Should publish OrderStatusChangedEvent")
    void shouldPublishOrderStatusChangedEvent() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ORDER_ID)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        payOrderService.execute(PUBLIC_ORDER_ID);

        // Assert
        verify(eventPublisher).publish(argThat(event -> 
            event.getEventType().equals("OrderStatusChanged")));
    }
}
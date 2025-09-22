package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.example.pruebatecnicaecommerce.shared.error.InvalidOrderStateTransitionException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelOrderService Unit Tests")
class CancelOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CancelOrderService cancelOrderService;

    private Order order;
    private final String PUBLIC_ID = "ORD-123-ABC";
    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Arrange
        order = Order.create(CUSTOMER_ID);
        // Note: Order will have its own public ID generated automatically
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void should_CancelOrder_When_OrderIsPending() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = cancelOrderService.execute(PUBLIC_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED.toString());
        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository).save(order);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should cancel paid order and release inventory")
    void should_CancelOrder_When_OrderIsPaid() {
        // Arrange
        OrderItem orderItem = new OrderItem(PRODUCT_ID, 2, new BigDecimal("50.00"));
        order.addItem(orderItem); // Add item first while order is in CREATED state
        order.pay(); // Then set order to PAID status

        Inventory inventory = Inventory.create(PRODUCT_ID, 10);

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(inventory));
        doNothing().when(inventoryRepository).save(any(Inventory.class));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = cancelOrderService.execute(PUBLIC_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED.toString());

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(inventoryRepository).findByProductId(PRODUCT_ID);
        verify(inventoryRepository).save(inventory);
        verify(orderRepository).save(order);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cancelOrderService.execute(PUBLIC_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(PUBLIC_ID);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateTransitionException when order is already shipped")
    void should_ThrowInvalidOrderStateTransitionException_When_OrderIsShipped() {
        // Arrange
        order.pay();
        order.ship(); // Set order to SHIPPED status

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> cancelOrderService.execute(PUBLIC_ID))
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateTransitionException when order is already cancelled")
    void should_ThrowInvalidOrderStateTransitionException_When_OrderIsAlreadyCancelled() {
        // Arrange
        order.cancel(); // Set order to CANCELLED status

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> cancelOrderService.execute(PUBLIC_ID))
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
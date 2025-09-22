package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipOrderService Unit Tests")
class ShipOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ShipOrderService shipOrderService;

    private Order order;
    private final String PUBLIC_ID = "ORD-123-ABC";
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Arrange
        order = Order.create(CUSTOMER_ID);
    }

    @Test
    @DisplayName("Should ship paid order successfully")
    void should_ShipOrder_When_OrderIsPaid() {
        // Arrange
        order.pay(); // Set order to PAID status

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = shipOrderService.execute(PUBLIC_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED.toString());
        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository).save(order);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        // Arrange
        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> shipOrderService.execute(PUBLIC_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(PUBLIC_ID);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateTransitionException when order is pending")
    void should_ThrowInvalidOrderStateTransitionException_When_OrderIsPending() {
        // Arrange - Order is in PENDING state by default
        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> shipOrderService.execute(PUBLIC_ID))
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateTransitionException when order is already shipped")
    void should_ThrowInvalidOrderStateTransitionException_When_OrderIsAlreadyShipped() {
        // Arrange
        order.pay();
        order.ship(); // Set order to SHIPPED status

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> shipOrderService.execute(PUBLIC_ID))
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateTransitionException when order is cancelled")
    void should_ThrowInvalidOrderStateTransitionException_When_OrderIsCancelled() {
        // Arrange
        order.cancel(); // Set order to CANCELLED status

        when(orderRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> shipOrderService.execute(PUBLIC_ID))
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        verify(orderRepository).findByPublicId(PUBLIC_ID);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
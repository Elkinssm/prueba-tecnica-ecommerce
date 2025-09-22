package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderStatusHistoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderStatusHistoryEntity;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Query Services Unit Tests")
class OrderQueryServicesTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private JpaOrderStatusHistoryRepository historyRepository;

        @InjectMocks
        private GetOrderService getOrderService;

        @InjectMocks
        private ListOrdersService listOrdersService;

        @InjectMocks
        private GetOrderStatusService getOrderStatusService;

        @InjectMocks
        private GetOrderHistoryService getOrderHistoryService;

        private Order order1, order2, order3;
        private final String PUBLIC_ORDER_ID = "ORD-12345";
        private final UUID CUSTOMER_ID = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                order1 = Order.restore(
                                UUID.randomUUID(),
                                PUBLIC_ORDER_ID,
                                CUSTOMER_ID,
                                OrderStatus.CREATED,
                                Instant.now().minusSeconds(3600),
                                0);

                order2 = Order.restore(
                                UUID.randomUUID(),
                                "ORD-67890",
                                CUSTOMER_ID,
                                OrderStatus.PAID,
                                Instant.now().minusSeconds(1800),
                                0);

                order3 = Order.restore(
                                UUID.randomUUID(),
                                "ORD-11111",
                                UUID.randomUUID(),
                                OrderStatus.SHIPPED,
                                Instant.now().minusSeconds(900),
                                0);
        }

        // GetOrderService Tests
        @Test
        @DisplayName("GetOrderService: Should return order when found")
        void getOrderService_shouldReturnOrderWhenFound() {
                // Arrange
                when(orderRepository.findByPublicId(eq(PUBLIC_ORDER_ID)))
                                .thenReturn(Optional.of(order1));

                // Act
                OrderResponse result = getOrderService.execute(PUBLIC_ORDER_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(PUBLIC_ORDER_ID);
                assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
                assertThat(result.getStatus()).isEqualTo("CREATED");

                verify(orderRepository).findByPublicId(PUBLIC_ORDER_ID);
        }

        @Test
        @DisplayName("GetOrderService: Should throw exception when order not found")
        void getOrderService_shouldThrowExceptionWhenOrderNotFound() {
                // Arrange
                String nonExistentOrderId = "NON-EXISTENT";
                when(orderRepository.findByPublicId(eq(nonExistentOrderId)))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> getOrderService.execute(nonExistentOrderId))
                                .isInstanceOf(OrderNotFoundException.class)
                                .hasMessage("Order not found with public ID: " + nonExistentOrderId);

                verify(orderRepository).findByPublicId(nonExistentOrderId);
        }

        // ListOrdersService Tests
        @Test
        @DisplayName("ListOrdersService: Should return all orders")
        void listOrdersService_shouldReturnAllOrders() {
                // Arrange
                when(orderRepository.findAll())
                                .thenReturn(Arrays.asList(order1, order2, order3));

                // Act
                List<OrderResponse> result = listOrdersService.execute();

                // Assert
                assertThat(result).hasSize(3);
                assertThat(result).extracting(OrderResponse::getId)
                                .containsExactly(PUBLIC_ORDER_ID, "ORD-67890", "ORD-11111");
                assertThat(result).extracting(OrderResponse::getStatus)
                                .containsExactly("CREATED", "PAID", "SHIPPED");

                verify(orderRepository).findAll();
        }

        @Test
        @DisplayName("ListOrdersService: Should return empty list when no orders exist")
        void listOrdersService_shouldReturnEmptyListWhenNoOrdersExist() {
                // Arrange
                when(orderRepository.findAll())
                                .thenReturn(Collections.emptyList());

                // Act
                List<OrderResponse> result = listOrdersService.execute();

                // Assert
                assertThat(result).isEmpty();

                verify(orderRepository).findAll();
        }

        // GetOrderStatusService Tests
        @Test
        @DisplayName("GetOrderStatusService: Should return order status when found")
        void getOrderStatusService_shouldReturnOrderStatusWhenFound() {
                // Arrange
                when(orderRepository.findByPublicId(eq(PUBLIC_ORDER_ID)))
                                .thenReturn(Optional.of(order1));

                // Act
                String result = getOrderStatusService.execute(PUBLIC_ORDER_ID);

                // Assert
                assertThat(result).isEqualTo("CREATED");

                verify(orderRepository).findByPublicId(PUBLIC_ORDER_ID);
        }

        @Test
        @DisplayName("GetOrderStatusService: Should throw exception when order not found")
        void getOrderStatusService_shouldThrowExceptionWhenOrderNotFound() {
                // Arrange
                String nonExistentOrderId = "NON-EXISTENT";
                when(orderRepository.findByPublicId(eq(nonExistentOrderId)))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> getOrderStatusService.execute(nonExistentOrderId))
                                .isInstanceOf(OrderNotFoundException.class)
                                .hasMessage("Order not found with public ID: " + nonExistentOrderId);

                verify(orderRepository).findByPublicId(nonExistentOrderId);
        }

        @Test
        @DisplayName("GetOrderStatusService: Should return different statuses correctly")
        void getOrderStatusService_shouldReturnDifferentStatusesCorrectly() {
                // Test PAID status
                when(orderRepository.findByPublicId(eq("ORD-PAID")))
                                .thenReturn(Optional.of(order2));

                String paidStatus = getOrderStatusService.execute("ORD-PAID");
                assertThat(paidStatus).isEqualTo("PAID");

                // Test SHIPPED status
                when(orderRepository.findByPublicId(eq("ORD-SHIPPED")))
                                .thenReturn(Optional.of(order3));

                String shippedStatus = getOrderStatusService.execute("ORD-SHIPPED");
                assertThat(shippedStatus).isEqualTo("SHIPPED");

                verify(orderRepository).findByPublicId("ORD-PAID");
                verify(orderRepository).findByPublicId("ORD-SHIPPED");
        }

        // GetOrderHistoryService Tests
        @Test
        @DisplayName("GetOrderHistoryService: Should return history for CREATED order")
        void getOrderHistoryService_shouldReturnHistoryForCreatedOrder() {
                // Arrange
                when(orderRepository.findByPublicId(eq(PUBLIC_ORDER_ID)))
                                .thenReturn(Optional.of(order1));

                // Mock empty history (no history entries found)
                when(historyRepository.findByOrderIdOrderByChangedAtAsc(order1.getId()))
                                .thenReturn(Collections.emptyList());

                // Act
                List<OrderStatusHistoryResponse> result = getOrderHistoryService.execute(PUBLIC_ORDER_ID);

                // Assert
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
                assertThat(result.get(0).getPreviousStatus()).isNull();
                assertThat(result.get(0).getChangedAt()).isNotNull();

                verify(orderRepository).findByPublicId(PUBLIC_ORDER_ID);
        }

        @Test
        @DisplayName("GetOrderHistoryService: Should return history for non-CREATED order")
        void getOrderHistoryService_shouldReturnHistoryForNonCreatedOrder() {
                // Arrange
                when(orderRepository.findByPublicId(eq("ORD-PAID")))
                                .thenReturn(Optional.of(order2));

                // Mock history entities
                OrderStatusHistoryEntity historyEntity1 = new OrderStatusHistoryEntity();
                historyEntity1.setOrderId(order2.getId());
                historyEntity1.setNewStatus(OrderStatus.CREATED);
                historyEntity1.setPreviousStatus(null);
                historyEntity1.setChangedAt(Instant.now().minusSeconds(3600));

                OrderStatusHistoryEntity historyEntity2 = new OrderStatusHistoryEntity();
                historyEntity2.setOrderId(order2.getId());
                historyEntity2.setNewStatus(OrderStatus.PAID);
                historyEntity2.setPreviousStatus(OrderStatus.CREATED);
                historyEntity2.setChangedAt(Instant.now());

                when(historyRepository.findByOrderIdOrderByChangedAtAsc(order2.getId()))
                                .thenReturn(Arrays.asList(historyEntity1, historyEntity2));

                // Act
                List<OrderStatusHistoryResponse> result = getOrderHistoryService.execute("ORD-PAID");

                // Assert
                assertThat(result).hasSize(2);

                // First entry should be CREATED
                assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
                assertThat(result.get(0).getPreviousStatus()).isNull();

                // Second entry should be current status
                assertThat(result.get(1).getStatus()).isEqualTo("PAID");
                assertThat(result.get(1).getPreviousStatus()).isEqualTo("CREATED");
                assertThat(result.get(1).getChangedAt()).isNotNull();

                verify(orderRepository).findByPublicId("ORD-PAID");
        }

        @Test
        @DisplayName("GetOrderHistoryService: Should throw exception when order not found")
        void getOrderHistoryService_shouldThrowExceptionWhenOrderNotFound() {
                // Arrange
                String nonExistentOrderId = "NON-EXISTENT";
                when(orderRepository.findByPublicId(eq(nonExistentOrderId)))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> getOrderHistoryService.execute(nonExistentOrderId))
                                .isInstanceOf(OrderNotFoundException.class)
                                .hasMessage("Order not found with public ID: " + nonExistentOrderId);

                verify(orderRepository).findByPublicId(nonExistentOrderId);
        }

        @Test
        @DisplayName("GetOrderHistoryService: Should handle SHIPPED order correctly")
        void getOrderHistoryService_shouldHandleShippedOrderCorrectly() {
                // Arrange
                when(orderRepository.findByPublicId(eq("ORD-SHIPPED")))
                                .thenReturn(Optional.of(order3));

                // Mock history entities
                OrderStatusHistoryEntity historyEntity1 = new OrderStatusHistoryEntity();
                historyEntity1.setOrderId(order3.getId());
                historyEntity1.setNewStatus(OrderStatus.CREATED);
                historyEntity1.setPreviousStatus(null);
                historyEntity1.setChangedAt(Instant.now().minusSeconds(7200));

                OrderStatusHistoryEntity historyEntity2 = new OrderStatusHistoryEntity();
                historyEntity2.setOrderId(order3.getId());
                historyEntity2.setNewStatus(OrderStatus.SHIPPED);
                historyEntity2.setPreviousStatus(OrderStatus.CREATED);
                historyEntity2.setChangedAt(Instant.now());

                when(historyRepository.findByOrderIdOrderByChangedAtAsc(order3.getId()))
                                .thenReturn(Arrays.asList(historyEntity1, historyEntity2));

                // Act
                List<OrderStatusHistoryResponse> result = getOrderHistoryService.execute("ORD-SHIPPED");

                // Assert
                assertThat(result).hasSize(2);

                // First entry should be CREATED
                assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
                assertThat(result.get(0).getPreviousStatus()).isNull();

                // Second entry should be SHIPPED
                assertThat(result.get(1).getStatus()).isEqualTo("SHIPPED");
                assertThat(result.get(1).getPreviousStatus()).isEqualTo("CREATED");

                verify(orderRepository).findByPublicId("ORD-SHIPPED");
        }
}
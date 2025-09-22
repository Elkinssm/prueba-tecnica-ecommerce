package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderFilterCriteria;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchOrdersService Unit Tests")
class SearchOrdersServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SearchOrdersService searchOrdersService;

    private UUID customerId;
    private Order order1, order2, order3;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        
        order1 = Order.restore(
            UUID.randomUUID(),
            "ORD-001",
            customerId,
            OrderStatus.CREATED,
            Instant.now().minusSeconds(3600),
            0
        );
        
        order2 = Order.restore(
            UUID.randomUUID(),
            "ORD-002",
            customerId,
            OrderStatus.PAID,
            Instant.now().minusSeconds(1800),
            0
        );
        
        order3 = Order.restore(
            UUID.randomUUID(),
            "ORD-003",
            UUID.randomUUID(), // Different customer
            OrderStatus.SHIPPED,
            Instant.now().minusSeconds(900),
            0
        );
    }

    @Test
    @DisplayName("Should search orders by customer ID and status")
    void shouldSearchOrdersByCustomerIdAndStatus() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(customerId.toString())
                .status("CREATED")
                .build();

        when(orderRepository.findByCustomerIdAndStatus(eq(customerId), eq(OrderStatus.CREATED)))
                .thenReturn(Arrays.asList(order1));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-001");
        assertThat(result.get(0).getCustomerId()).isEqualTo(customerId.toString());
        assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
        
        verify(orderRepository).findByCustomerIdAndStatus(customerId, OrderStatus.CREATED);
        verify(orderRepository, never()).findByCustomerId(any());
        verify(orderRepository, never()).findByStatus(any());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, never()).findByCreatedAtBetween(any(), any());
    }

    @Test
    @DisplayName("Should search orders by customer ID only")
    void shouldSearchOrdersByCustomerIdOnly() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(customerId.toString())
                .build();

        when(orderRepository.findByCustomerId(eq(customerId)))
                .thenReturn(Arrays.asList(order1, order2));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponse::getId)
                .containsExactly("ORD-001", "ORD-002");
        
        verify(orderRepository).findByCustomerId(customerId);
        verify(orderRepository, never()).findByCustomerIdAndStatus(any(), any());
        verify(orderRepository, never()).findByStatus(any());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, never()).findByCreatedAtBetween(any(), any());
    }

    @Test
    @DisplayName("Should search orders by status only")
    void shouldSearchOrdersByStatusOnly() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .status("SHIPPED")
                .build();

        when(orderRepository.findByStatus(eq(OrderStatus.SHIPPED)))
                .thenReturn(Arrays.asList(order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-003");
        assertThat(result.get(0).getStatus()).isEqualTo("SHIPPED");
        
        verify(orderRepository).findByStatus(OrderStatus.SHIPPED);
        verify(orderRepository, never()).findByCustomerId(any());
        verify(orderRepository, never()).findByCustomerIdAndStatus(any(), any());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, never()).findByCreatedAtBetween(any(), any());
    }

    @Test
    @DisplayName("Should search orders by date range")
    void shouldSearchOrdersByDateRange() {
        // Arrange
        LocalDateTime dateFrom = LocalDateTime.now().minusHours(2);
        LocalDateTime dateTo = LocalDateTime.now();
        
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        when(orderRepository.findByCreatedAtBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponse::getId)
                .containsExactly("ORD-002", "ORD-003");
        
        verify(orderRepository).findByCreatedAtBetween(any(Instant.class), any(Instant.class));
        verify(orderRepository, never()).findByCustomerId(any());
        verify(orderRepository, never()).findByCustomerIdAndStatus(any(), any());
        verify(orderRepository, never()).findByStatus(any());
        verify(orderRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should search all orders when no criteria provided")
    void shouldSearchAllOrdersWhenNoCriteriaProvided() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder().build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(OrderResponse::getId)
                .containsExactly("ORD-001", "ORD-002", "ORD-003");
        
        verify(orderRepository).findAll();
        verify(orderRepository, never()).findByCustomerId(any());
        verify(orderRepository, never()).findByCustomerIdAndStatus(any(), any());
        verify(orderRepository, never()).findByStatus(any());
        verify(orderRepository, never()).findByCreatedAtBetween(any(), any());
    }

    @Test
    @DisplayName("Should apply search filter to orders by public ID")
    void shouldApplySearchFilterByPublicId() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search("ORD-002")
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-002");
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should apply search filter to orders by status")
    void shouldApplySearchFilterByStatus() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search("shipped")
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-003");
        assertThat(result.get(0).getStatus()).isEqualTo("SHIPPED");
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when search term not found")
    void shouldReturnEmptyListWhenSearchTermNotFound() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search("NON-EXISTENT")
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).isEmpty();
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should combine customer filter with search filter")
    void shouldCombineCustomerFilterWithSearchFilter() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(customerId.toString())
                .search("ORD-002")
                .build();

        when(orderRepository.findByCustomerId(eq(customerId)))
                .thenReturn(Arrays.asList(order1, order2));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-002");
        
        verify(orderRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should handle empty search string")
    void shouldHandleEmptySearchString() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search("   ")
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(3);
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should handle null search string")
    void shouldHandleNullSearchString() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search(null)
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(3);
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no orders found")
    void shouldReturnEmptyListWhenNoOrdersFound() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .customerId(UUID.randomUUID().toString())
                .build();

        when(orderRepository.findByCustomerId(any(UUID.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).isEmpty();
        
        verify(orderRepository).findByCustomerId(any(UUID.class));
    }

    @Test
    @DisplayName("Should search with case insensitive search term")
    void shouldSearchWithCaseInsensitiveSearchTerm() {
        // Arrange
        OrderFilterCriteria criteria = OrderFilterCriteria.builder()
                .search("CREATED")
                .build();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<OrderResponse> result = searchOrdersService.execute(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ORD-001");
        assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
        
        verify(orderRepository).findAll();
    }
}
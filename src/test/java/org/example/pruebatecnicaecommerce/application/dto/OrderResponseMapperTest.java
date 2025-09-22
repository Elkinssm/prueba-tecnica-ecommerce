package org.example.pruebatecnicaecommerce.application.dto;

import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderResponseMapper Unit Tests")
class OrderResponseMapperTest {

    private Order order;
    private final UUID CUSTOMER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID PRODUCT_ID_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID PRODUCT_ID_2 = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    @BeforeEach
    void setUp() {
        // Arrange - Set up test data
        order = Order.create(CUSTOMER_ID);
        order.addItem(new OrderItem(PRODUCT_ID_1, 2, new BigDecimal("29.99")));
        order.addItem(new OrderItem(PRODUCT_ID_2, 1, new BigDecimal("15.50")));
    }

    @Test
    @DisplayName("Should map order to response correctly")
    void shouldMapOrderToResponseCorrectly() {
        // Arrange
        // Order already set up in @BeforeEach

        // Act
        OrderResponse result = OrderResponseMapper.fromDomain(order);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
        assertThat(result.getStatus()).isEqualTo("CREATED");
        assertThat(result.getOrderDate()).isNotNull();
        assertThat(result.getTotal()).isEqualTo(new BigDecimal("75.48"));
    }

    @Test
    @DisplayName("Should map order items correctly")
    void shouldMapOrderItemsCorrectly() {
        // Arrange
        // Order already set up in @BeforeEach

        // Act
        OrderResponse result = OrderResponseMapper.fromDomain(order);

        // Assert
        assertThat(result.getItems()).hasSize(2);
        
        ItemResponse firstItem = result.getItems().get(0);
        assertThat(firstItem.getProductId()).isEqualTo(PRODUCT_ID_1.toString());
        assertThat(firstItem.getQuantity()).isEqualTo(2);
        assertThat(firstItem.getUnitPrice()).isEqualTo(new BigDecimal("29.99"));
        
        ItemResponse secondItem = result.getItems().get(1);
        assertThat(secondItem.getProductId()).isEqualTo(PRODUCT_ID_2.toString());
        assertThat(secondItem.getQuantity()).isEqualTo(1);
        assertThat(secondItem.getUnitPrice()).isEqualTo(new BigDecimal("15.50"));
    }

    @Test
    @DisplayName("Should calculate total correctly")
    void shouldCalculateTotalCorrectly() {
        // Arrange
        // Order already set up in @BeforeEach

        // Act
        OrderResponse result = OrderResponseMapper.fromDomain(order);

        // Assert
        // Expected total: (2 * 29.99) + (1 * 15.50) = 59.98 + 15.50 = 75.48
        assertThat(result.getTotal()).isEqualTo(new BigDecimal("75.48"));
    }

    @Test
    @DisplayName("Should handle empty order items")
    void shouldHandleEmptyOrderItems() {
        // Arrange
        Order emptyOrder = Order.create(CUSTOMER_ID);

        // Act
        OrderResponse result = OrderResponseMapper.fromDomain(emptyOrder);

        // Assert
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should use public ID for API response")
    void shouldUsePublicIdForApiResponse() {
        // Arrange
        // Order already set up in @BeforeEach

        // Act
        OrderResponse result = OrderResponseMapper.fromDomain(order);

        // Assert
        assertThat(result.getId()).isNotEqualTo(order.getId().toString());
        assertThat(result.getId()).isEqualTo(order.getPublicId());
        assertThat(result.getId()).startsWith("ORD-");
    }
}
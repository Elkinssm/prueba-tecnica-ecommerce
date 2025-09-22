package org.example.pruebatecnicaecommerce.domain.model.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderCreatedEvent Unit Tests")
class OrderCreatedEventTest {

    private UUID orderId;
    private String publicOrderId;
    private UUID customerId;
    private BigDecimal orderTotal;
    private int itemCount;
    private OrderCreatedEvent event;

    @BeforeEach
    void setUp() {
        // Arrange - Set up test data
        orderId = UUID.randomUUID();
        publicOrderId = "ORD-20250921-TEST";
        customerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        orderTotal = new BigDecimal("75.48");
        itemCount = 2;
    }

    @Test
    @DisplayName("Should create event with correct data")
    void shouldCreateEventWithCorrectData() {
        // Arrange
        // Test data already set up in @BeforeEach

        // Act
        event = new OrderCreatedEvent(orderId, publicOrderId, customerId, orderTotal, itemCount);

        // Assert
        assertThat(event.getAggregateId()).isEqualTo(orderId);
        assertThat(event.getPublicOrderId()).isEqualTo(publicOrderId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getOrderTotal()).isEqualTo(orderTotal);
        assertThat(event.getItemCount()).isEqualTo(itemCount);
        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(event.getAggregateType()).isEqualTo("Order");
    }

    @Test
    @DisplayName("Should have timestamp when created")
    void shouldHaveTimestampWhenCreated() {
        // Arrange
        Instant beforeCreation = Instant.now();

        // Act
        event = new OrderCreatedEvent(orderId, publicOrderId, customerId, orderTotal, itemCount);

        // Assert
        Instant afterCreation = Instant.now();
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isBetween(beforeCreation, afterCreation);
    }

    @Test
    @DisplayName("Should generate unique event ID")
    void shouldGenerateUniqueEventId() {
        // Arrange & Act
        OrderCreatedEvent event1 = new OrderCreatedEvent(orderId, publicOrderId, customerId, orderTotal, itemCount);
        OrderCreatedEvent event2 = new OrderCreatedEvent(orderId, publicOrderId, customerId, orderTotal, itemCount);

        // Assert
        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    @DisplayName("Should be immutable after creation")
    void shouldBeImmutableAfterCreation() {
        // Arrange & Act
        event = new OrderCreatedEvent(orderId, publicOrderId, customerId, orderTotal, itemCount);

        // Assert
        assertThat(event.getAggregateId()).isEqualTo(orderId);
        assertThat(event.getPublicOrderId()).isEqualTo(publicOrderId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getOrderTotal()).isEqualTo(orderTotal);
        assertThat(event.getItemCount()).isEqualTo(itemCount);

        // Verify immutability by checking that getters return the same values
        UUID retrievedOrderId = event.getAggregateId();
        String retrievedPublicId = event.getPublicOrderId();
        assertThat(retrievedOrderId).isEqualTo(orderId);
        assertThat(retrievedPublicId).isEqualTo(publicOrderId);
    }
}
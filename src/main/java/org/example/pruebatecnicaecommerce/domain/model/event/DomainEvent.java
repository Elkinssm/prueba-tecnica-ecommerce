package org.example.pruebatecnicaecommerce.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events
 */
public interface DomainEvent {
    UUID getEventId();

    Instant getOccurredAt();

    String getEventType();

    UUID getAggregateId();

    String getAggregateType();
}
package org.example.pruebatecnicaecommerce.domain.model.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base class for domain events
 */
@Getter
public abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final UUID aggregateId;
    private final String aggregateType;

    protected BaseDomainEvent(UUID aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }
}
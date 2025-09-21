package org.example.pruebatecnicaecommerce.domain.service;

import org.example.pruebatecnicaecommerce.domain.model.event.DomainEvent;

/**
 * Service for publishing domain events
 */
public interface EventPublisher {
    void publish(DomainEvent event);
}
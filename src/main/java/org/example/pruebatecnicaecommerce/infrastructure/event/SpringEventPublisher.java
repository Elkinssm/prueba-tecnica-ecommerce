package org.example.pruebatecnicaecommerce.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pruebatecnicaecommerce.domain.model.event.DomainEvent;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Spring-based implementation of EventPublisher
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing domain event: {} for aggregate: {}",
                event.getEventType(), event.getAggregateId());

        applicationEventPublisher.publishEvent(event);

        log.debug("Domain event published successfully: {}", event.getEventId());
    }
}
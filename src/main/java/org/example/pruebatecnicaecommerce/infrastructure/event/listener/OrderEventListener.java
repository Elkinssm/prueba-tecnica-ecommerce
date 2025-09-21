package org.example.pruebatecnicaecommerce.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pruebatecnicaecommerce.domain.model.event.OrderCreatedEvent;
import org.example.pruebatecnicaecommerce.domain.model.event.OrderStatusChangedEvent;
import org.example.pruebatecnicaecommerce.infrastructure.notification.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for order events that triggers notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for order: {}", event.getPublicOrderId());

        notificationService.sendOrderCreatedNotification(
                event.getCustomerId().toString(),
                event.getPublicOrderId(),
                event.getOrderTotal().toString());
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Processing OrderStatusChangedEvent for order: {} - {} -> {}",
                event.getPublicOrderId(), event.getPreviousStatus(), event.getNewStatus());

        notificationService.sendOrderStatusChangedNotification(
                event.getCustomerId().toString(),
                event.getPublicOrderId(),
                event.getPreviousStatus() != null ? event.getPreviousStatus().name() : "NONE",
                event.getNewStatus().name());

        switch (event.getNewStatus()) {
            case PAID:
                notificationService.sendOrderPaidNotification(
                        event.getCustomerId().toString(),
                        event.getPublicOrderId(),
                        event.getOrderTotal().toString());
                break;
            case SHIPPED:
                notificationService.sendOrderShippedNotification(
                        event.getCustomerId().toString(),
                        event.getPublicOrderId());
                break;
            case CANCELLED:
                notificationService.sendOrderCancelledNotification(
                        event.getCustomerId().toString(),
                        event.getPublicOrderId());
                break;
            default:
                break;
        }
    }
}
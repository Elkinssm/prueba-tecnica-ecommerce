package org.example.pruebatecnicaecommerce.infrastructure.notification;

/**
 * Service for sending notifications
 */
public interface NotificationService {
    void sendOrderCreatedNotification(String customerId, String orderId, String orderTotal);

    void sendOrderStatusChangedNotification(String customerId, String orderId, String previousStatus, String newStatus);

    void sendOrderPaidNotification(String customerId, String orderId, String orderTotal);

    void sendOrderShippedNotification(String customerId, String orderId);

    void sendOrderCancelledNotification(String customerId, String orderId);
}
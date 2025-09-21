package org.example.pruebatecnicaecommerce.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Mock implementation of NotificationService for testing purposes
 * In production, this would integrate with real email/SMS providers
 */
@Service
@Slf4j
public class MockNotificationService implements NotificationService {

    private final Random random = new Random();
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY = 1000L;

    @Override
    @Async("eventTaskExecutor")
    public void sendOrderCreatedNotification(String customerId, String orderId, String orderTotal) {
        executeWithRetry(() -> {
            simulateNetworkCall();
            log.info("📧 Notification sent: Order {} created for customer {} with total {}",
                    orderId, customerId, orderTotal);
        }, "OrderCreated notification for order " + orderId);
    }

    @Override
    @Async("eventTaskExecutor")
    public void sendOrderStatusChangedNotification(String customerId, String orderId,
            String previousStatus, String newStatus) {
        executeWithRetry(() -> {
            simulateNetworkCall();
            log.info("📧 Notification sent: Order {} status changed from {} to {} for customer {}",
                    orderId, previousStatus, newStatus, customerId);
        }, "OrderStatusChanged notification for order " + orderId);
    }

    @Override
    @Async("eventTaskExecutor")
    public void sendOrderPaidNotification(String customerId, String orderId, String orderTotal) {
        executeWithRetry(() -> {
            simulateNetworkCall();
            log.info("💰 Notification sent: Order {} paid by customer {} for total {}",
                    orderId, customerId, orderTotal);
        }, "OrderPaid notification for order " + orderId);
    }

    @Override
    @Async("eventTaskExecutor")
    public void sendOrderShippedNotification(String customerId, String orderId) {
        executeWithRetry(() -> {
            simulateNetworkCall();
            log.info("🚚 Notification sent: Order {} shipped to customer {}", orderId, customerId);
        }, "OrderShipped notification for order " + orderId);
    }

    @Override
    @Async("eventTaskExecutor")
    public void sendOrderCancelledNotification(String customerId, String orderId) {
        executeWithRetry(() -> {
            simulateNetworkCall();
            log.info("❌ Notification sent: Order {} cancelled for customer {}", orderId, customerId);
        }, "OrderCancelled notification for order " + orderId);
    }

    private void executeWithRetry(Runnable operation, String operationName) {
        int attempt = 1;

        while (attempt <= MAX_RETRIES) {
            try {
                operation.run();
                return; // Success
            } catch (NotificationException e) {
                log.warn("Attempt {} failed for {}: {}", attempt, operationName, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    log.error("All {} attempts failed for {}", MAX_RETRIES, operationName, e);
                    return; // Give up after max retries
                }

                try {
                    long delay = BASE_DELAY * (long) Math.pow(2, attempt - 1); // Exponential backoff
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for {}", operationName, ie);
                    return;
                }

                attempt++;
            }
        }
    }

    private void simulateNetworkCall() {
        try {
            Thread.sleep(100 + random.nextInt(200));

            // Simulate occasional failures for testing retry mechanism
            if (random.nextInt(100) < 15) { // 15% failure rate for testing
                throw new NotificationException("Simulated network failure");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NotificationException("Network call interrupted", e);
        }
    }
}
package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to "payment-events" topic.
 * Creates a notification when a payment is processed (COMPLETED or FAILED).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        String message = String.format("Payment %s for Order #%d. Amount: $%s. Payment ID: %d",
                event.getStatus(), event.getOrderId(),
                event.getAmount(), event.getPaymentId());

        notificationService.createNotification(
                NotificationType.PAYMENT,
                event.getOrderId(),
                message,
                event.getStatus(),
                event.getCustomerId()
        );
    }
}

package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;

    public PaymentEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
                event.getCustomerId());
    }
}

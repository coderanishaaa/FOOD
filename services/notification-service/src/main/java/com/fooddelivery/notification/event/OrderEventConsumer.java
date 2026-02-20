package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to "order-events" topic.
 * Creates a notification whenever an order is placed or its status changes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        String message = String.format("Order #%d status: %s. Amount: $%s. Delivery to: %s",
                event.getOrderId(), event.getStatus(),
                event.getTotalAmount(), event.getDeliveryAddress());

        notificationService.createNotification(
                NotificationType.ORDER,
                event.getOrderId(),
                message,
                event.getStatus(),
                event.getCustomerId()
        );
    }
}

package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to "delivery-events" topic.
 * Creates a notification when delivery status changes (ASSIGNED, PICKED_UP, DELIVERED).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
    public void consumeDeliveryEvent(DeliveryEvent event) {
        log.info("Received delivery event: deliveryId={}, orderId={}, status={}",
                event.getDeliveryId(), event.getOrderId(), event.getStatus());

        String message = String.format("Delivery #%d for Order #%d: %s. Agent: %d. Address: %s",
                event.getDeliveryId(), event.getOrderId(), event.getStatus(),
                event.getDeliveryAgentId(), event.getDeliveryAddress());

        // Delivery notifications target the delivery agent
        notificationService.createNotification(
                NotificationType.DELIVERY,
                event.getOrderId(),
                message,
                event.getStatus(),
                event.getDeliveryAgentId()
        );
    }
}

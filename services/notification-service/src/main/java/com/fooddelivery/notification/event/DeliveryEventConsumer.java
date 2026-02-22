package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

        private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

        private final NotificationService notificationService;

        public DeliveryEventConsumer(NotificationService notificationService) {
                this.notificationService = notificationService;
        }

        @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
        public void consumeDeliveryEvent(DeliveryEvent event) {
                log.info("Received delivery event: deliveryId={}, orderId={}, status={}",
                                event.getDeliveryId(), event.getOrderId(), event.getStatus());

                String message = String.format("Delivery #%d for Order #%d: %s. Agent: %d. Address: %s",
                                event.getDeliveryId(), event.getOrderId(), event.getStatus(),
                                event.getDeliveryAgentId(), event.getDeliveryAddress());

                notificationService.createNotification(
                                NotificationType.DELIVERY,
                                event.getOrderId(),
                                message,
                                event.getStatus(),
                                event.getDeliveryAgentId());
        }
}

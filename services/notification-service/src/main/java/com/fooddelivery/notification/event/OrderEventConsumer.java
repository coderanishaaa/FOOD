package com.fooddelivery.notification.event;

import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
                event.getCustomerId());
    }
}

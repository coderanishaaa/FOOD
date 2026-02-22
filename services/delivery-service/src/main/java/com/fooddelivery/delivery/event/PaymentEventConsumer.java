package com.fooddelivery.delivery.event;

import com.fooddelivery.delivery.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for payment-events. When payment is COMPLETED,
 * creates a delivery record and assigns a delivery agent.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final DeliveryService deliveryService;

    public PaymentEventConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "payment-events", groupId = "delivery-service-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        if ("COMPLETED".equals(event.getStatus())) {
            deliveryService.createDelivery(event);
        }
    }
}

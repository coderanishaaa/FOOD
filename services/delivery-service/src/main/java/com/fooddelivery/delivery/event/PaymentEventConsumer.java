package com.fooddelivery.delivery.event;

import com.fooddelivery.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for payment-events. When payment is COMPLETED,
 * creates a delivery record and assigns a delivery agent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "payment-events", groupId = "delivery-service-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        if ("COMPLETED".equals(event.getStatus())) {
            deliveryService.createDelivery(event);
        }
    }
}

package com.fooddelivery.payment.event;

import com.fooddelivery.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for order-events.
 * When an order is placed, automatically triggers payment processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        // Only process PLACED orders
        if ("PLACED".equals(event.getStatus())) {
            paymentService.processPayment(event);
        }
    }
}

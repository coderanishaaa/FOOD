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

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        // Create payment record with PENDING status when order is created
        // Payment will be processed via Stripe checkout
        if ("PENDING_PAYMENT".equals(event.getStatus())) {
            paymentService.createPaymentRecord(event);
        }
    }
}

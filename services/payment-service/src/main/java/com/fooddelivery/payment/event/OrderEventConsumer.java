package com.fooddelivery.payment.event;

import com.fooddelivery.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for order-events.
 * When an order is placed, automatically triggers payment processing.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentService paymentService;

    public OrderEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        if ("PENDING_PAYMENT".equals(event.getStatus())) {
            paymentService.createPaymentRecord(event);
        }
    }
}

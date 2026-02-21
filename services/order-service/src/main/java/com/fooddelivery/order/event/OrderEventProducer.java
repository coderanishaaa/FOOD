package com.fooddelivery.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer that publishes OrderEvent to the "order-events" topic.
 * Consumed by payment-service and notification-service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "order-created-topic";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event: orderId={}, status={}", event.getOrderId(), event.getStatus());
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Order event published to topic={}, partition={}",
                                    TOPIC, result.getRecordMetadata().partition());
                        } else {
                            log.error("Failed to publish order event: orderId={}", event.getOrderId(), ex);
                        }
                    });
        } catch (Exception ex) {
            // Swallow exceptions from the producer so order creation isn't rolled back when Kafka is down
            log.error("Exception while publishing order event (non-fatal): orderId={}", event.getOrderId(), ex);
        }
    }
}

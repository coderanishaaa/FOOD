package com.fooddelivery.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-created-topic";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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
            log.error("Exception while publishing order event (non-fatal): orderId={}", event.getOrderId(), ex);
        }
    }
}

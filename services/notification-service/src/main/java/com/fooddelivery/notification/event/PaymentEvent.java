package com.fooddelivery.notification.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirrors the PaymentEvent from payment-service.
 * Used to deserialize Kafka messages from "payment-events" topic.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

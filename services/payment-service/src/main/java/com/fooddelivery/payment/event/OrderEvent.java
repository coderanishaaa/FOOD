package com.fooddelivery.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirrors the OrderEvent from order-service.
 * Used to deserialize Kafka messages from "order-events" topic.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal totalAmount;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

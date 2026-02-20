package com.fooddelivery.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event published when an order is placed or its status changes.
 * Consumed by payment-service, notification-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal totalAmount;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

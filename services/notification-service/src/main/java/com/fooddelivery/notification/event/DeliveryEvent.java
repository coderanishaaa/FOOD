package com.fooddelivery.notification.event;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Mirrors the DeliveryEvent from delivery-service.
 * Used to deserialize Kafka messages from "delivery-events" topic.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryEvent {
    private Long deliveryId;
    private Long orderId;
    private Long deliveryAgentId;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

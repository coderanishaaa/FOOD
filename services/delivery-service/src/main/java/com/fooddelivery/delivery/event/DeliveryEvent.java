package com.fooddelivery.delivery.event;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryEvent {
    private Long deliveryId;
    private Long orderId;
    private Long deliveryAgentId;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

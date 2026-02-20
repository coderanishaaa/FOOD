package com.fooddelivery.delivery.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryDto {
    private Long id;
    private Long orderId;
    private Long deliveryAgentId;
    private String deliveryAddress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

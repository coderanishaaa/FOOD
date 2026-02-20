package com.fooddelivery.notification.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDto {
    private Long id;
    private String type;
    private Long orderId;
    private String message;
    private String status;
    private Long recipientId;
    private Boolean readStatus;
    private LocalDateTime createdAt;
}

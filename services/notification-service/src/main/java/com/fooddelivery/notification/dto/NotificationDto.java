package com.fooddelivery.notification.dto;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private String type;
    private Long orderId;
    private String message;
    private String status;
    private Long recipientId;
    private Boolean readStatus;
    private LocalDateTime createdAt;

    public NotificationDto() {
    }

    public NotificationDto(Long id, String type, Long orderId, String message, String status, Long recipientId,
            Boolean readStatus, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.orderId = orderId;
        this.message = message;
        this.status = status;
        this.recipientId = recipientId;
        this.readStatus = readStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Boolean getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

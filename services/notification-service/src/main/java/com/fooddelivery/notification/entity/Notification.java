package com.fooddelivery.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores notification log entries.
 * Each Kafka event consumed produces a Notification record.
 */
@Entity
@Table(name = "notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of event: ORDER, PAYMENT, DELIVERY */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** The order this notification relates to */
    @Column(nullable = false)
    private Long orderId;

    /** Human-readable notification message */
    @Column(nullable = false, length = 500)
    private String message;

    /** Raw event status value */
    @Column(nullable = false)
    private String status;

    /** Target user ID (customer, agent, etc.) — nullable for system-level events */
    private Long recipientId;

    /** Whether the notification has been read by the recipient */
    @Column(nullable = false)
    @Builder.Default
    private Boolean readStatus = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

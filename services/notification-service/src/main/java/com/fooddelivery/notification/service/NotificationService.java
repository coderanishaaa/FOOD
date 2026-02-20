package com.fooddelivery.notification.service;

import com.fooddelivery.notification.dto.NotificationDto;
import com.fooddelivery.notification.entity.Notification;
import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create and persist a notification record.
     * In production this would also push to WebSocket, email, SMS, etc.
     */
    @Transactional
    public NotificationDto createNotification(NotificationType type, Long orderId,
                                               String message, String status, Long recipientId) {
        Notification notification = Notification.builder()
                .type(type)
                .orderId(orderId)
                .message(message)
                .status(status)
                .recipientId(recipientId)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification saved: id={}, type={}, orderId={}, recipient={}",
                notification.getId(), type, orderId, recipientId);

        return mapToDto(notification);
    }

    public List<NotificationDto> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getNotificationsByRecipientId(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getUnreadNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdAndReadStatusFalseOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setReadStatus(true);
        notification = notificationRepository.save(notification);
        return mapToDto(notification);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType().name())
                .orderId(n.getOrderId())
                .message(n.getMessage())
                .status(n.getStatus())
                .recipientId(n.getRecipientId())
                .readStatus(n.getReadStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

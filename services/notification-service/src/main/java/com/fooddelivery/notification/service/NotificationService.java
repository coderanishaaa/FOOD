package com.fooddelivery.notification.service;

import com.fooddelivery.notification.dto.NotificationDto;
import com.fooddelivery.notification.entity.Notification;
import com.fooddelivery.notification.entity.NotificationType;
import com.fooddelivery.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

        private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

        private final NotificationRepository notificationRepository;

        public NotificationService(NotificationRepository notificationRepository) {
                this.notificationRepository = notificationRepository;
        }

        @Transactional
        public NotificationDto createNotification(NotificationType type, Long orderId,
                        String message, String status, Long recipientId) {
                Notification notification = new Notification();
                notification.setType(type);
                notification.setOrderId(orderId);
                notification.setMessage(message);
                notification.setStatus(status);
                notification.setRecipientId(recipientId);

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

        @Transactional
        public NotificationDto markAsRead(Long notificationId) {
                Notification notification = notificationRepository.findById(notificationId)
                                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
                notification.setReadStatus(true);
                notification = notificationRepository.save(notification);
                return mapToDto(notification);
        }

        private NotificationDto mapToDto(Notification n) {
                NotificationDto dto = new NotificationDto();
                dto.setId(n.getId());
                dto.setType(n.getType().name());
                dto.setOrderId(n.getOrderId());
                dto.setMessage(n.getMessage());
                dto.setStatus(n.getStatus());
                dto.setRecipientId(n.getRecipientId());
                dto.setReadStatus(n.getReadStatus());
                dto.setCreatedAt(n.getCreatedAt());
                return dto;
        }
}

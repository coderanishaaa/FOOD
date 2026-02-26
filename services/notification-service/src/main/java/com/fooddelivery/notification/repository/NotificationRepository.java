package com.fooddelivery.notification.repository;

import com.fooddelivery.notification.entity.Notification;
import com.fooddelivery.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Find all notifications for a specific order */
    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /** Find all notifications for a specific recipient (customer/agent) */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    /** Find notifications by type (ORDER, PAYMENT, DELIVERY) */
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    /** Find unread notifications for a recipient */
    List<Notification> findByRecipientIdAndReadStatusFalseOrderByCreatedAtDesc(Long recipientId);

    /** Delete all notifications for a recipient */
    void deleteByRecipientId(Long recipientId);
}

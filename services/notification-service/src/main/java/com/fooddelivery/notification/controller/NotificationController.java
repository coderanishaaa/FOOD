package com.fooddelivery.notification.controller;

import com.fooddelivery.notification.dto.ApiResponse;
import com.fooddelivery.notification.dto.NotificationDto;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Get all notifications (admin use) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getAllNotifications() {
        List<NotificationDto> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    /** Get notifications for a specific order */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByOrderId(@PathVariable Long orderId) {
        List<NotificationDto> notifications = notificationService.getNotificationsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Notifications for order " + orderId, notifications));
    }

    /** Get notifications for a specific user (customer/agent) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByUserId(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getNotificationsByRecipientId(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications for user " + userId, notifications));
    }

    /** Get unread notifications for a user */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnread(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications", notifications));
    }

    /** Mark a notification as read */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable Long id) {
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /** Clear all notifications for a user */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearNotifications(@PathVariable Long userId) {
        notificationService.clearNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications cleared", null));
    }
}

package com.fooddelivery.notification.controller;

import com.fooddelivery.notification.dto.ApiResponse;
import com.fooddelivery.notification.dto.NotificationDto;
import com.fooddelivery.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getAllNotifications() {
        List<NotificationDto> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByOrderId(@PathVariable Long orderId) {
        List<NotificationDto> notifications = notificationService.getNotificationsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Notifications for order " + orderId, notifications));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getByUserId(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getNotificationsByRecipientId(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications for user " + userId, notifications));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnread(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable Long id) {
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }
}

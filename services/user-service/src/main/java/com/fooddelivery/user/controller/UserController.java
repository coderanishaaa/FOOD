package com.fooddelivery.user.controller;

import com.fooddelivery.user.dto.ApiResponse;
import com.fooddelivery.user.dto.UserDto;
import com.fooddelivery.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Protected user management endpoints.
 * Gateway forwards X-User-Id, X-User-Email, X-User-Role headers after JWT
 * validation.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user's profile using the X-User-Id header from gateway.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @RequestHeader("X-User-Id") Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", user));
    }

    /**
     * Get user by ID (admin or self).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    /**
     * Get all users — admin only.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied. Admin role required."));
        }
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users retrieved", users));
    }

    /**
     * Get users by role — admin only.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByRole(
            @PathVariable String role,
            @RequestHeader("X-User-Role") String currentUserRole) {
        if (!"ADMIN".equals(currentUserRole)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied. Admin role required."));
        }
        List<UserDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved by role", users));
    }
}

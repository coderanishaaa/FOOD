package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.ApiResponse;
import com.fooddelivery.restaurant.dto.MenuItemDto;
import com.fooddelivery.restaurant.dto.MenuItemRequest;
import com.fooddelivery.restaurant.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemDto>> addMenuItem(
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        MenuItemDto item = menuItemService.addMenuItem(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item added", item));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemDto>>> getMenuItems(@PathVariable Long restaurantId) {
        List<MenuItemDto> items = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved", items));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemDto>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        MenuItemDto item = menuItemService.updateMenuItem(id, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated", item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        menuItemService.deleteMenuItem(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted", null));
    }
}

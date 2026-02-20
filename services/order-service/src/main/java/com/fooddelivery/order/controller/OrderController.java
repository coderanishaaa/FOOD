package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.ApiResponse;
import com.fooddelivery.order.dto.OrderRequest;
import com.fooddelivery.order.dto.OrderResponse;
import com.fooddelivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place a new order — customer only.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        OrderResponse order = orderService.placeOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    /**
     * Get all orders for the current customer.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomer(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved", orders));
    }

    /**
     * Get all orders for a restaurant (restaurant owner view).
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId) {
        List<OrderResponse> orders = orderService.getOrdersByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Restaurant orders retrieved", orders));
    }

    /**
     * Update order status (used internally / by admin).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }
}

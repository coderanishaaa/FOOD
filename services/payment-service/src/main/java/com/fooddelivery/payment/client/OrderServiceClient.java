package com.fooddelivery.payment.client;

import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OpenFeign client to communicate with order-service.
 * Used to fetch order details and update order status after payment.
 */
@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{orderId}")
    ApiResponse<OrderResponse> getOrderById(@PathVariable Long orderId);

    @PutMapping("/api/orders/{orderId}/status")
    ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status
    );
}

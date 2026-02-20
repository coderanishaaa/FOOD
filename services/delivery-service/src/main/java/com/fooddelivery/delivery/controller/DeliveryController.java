package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.ApiResponse;
import com.fooddelivery.delivery.dto.DeliveryDto;
import com.fooddelivery.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryDto delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery retrieved", delivery));
    }

    /**
     * Get all deliveries assigned to the current delivery agent.
     */
    @GetMapping("/my-deliveries")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getMyDeliveries(
            @RequestHeader("X-User-Id") Long agentId) {
        List<DeliveryDto> deliveries = deliveryService.getDeliveriesByAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent deliveries retrieved", deliveries));
    }

    /**
     * Update delivery status (delivery agent action).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DeliveryDto delivery = deliveryService.updateDeliveryStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated", delivery));
    }
}

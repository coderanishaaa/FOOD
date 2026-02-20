package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.ApiResponse;
import com.fooddelivery.restaurant.dto.RestaurantDto;
import com.fooddelivery.restaurant.dto.RestaurantRequest;
import com.fooddelivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantDto>> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        RestaurantDto restaurant = restaurantService.createRestaurant(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant created", restaurant));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantDto>>> getAllRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved", restaurants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDto>> getRestaurantById(@PathVariable Long id) {
        RestaurantDto restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved", restaurant));
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<RestaurantDto>>> getMyRestaurants(
            @RequestHeader("X-User-Id") Long userId) {
        List<RestaurantDto> restaurants = restaurantService.getRestaurantsByOwner(userId);
        return ResponseEntity.ok(ApiResponse.success("Owner restaurants retrieved", restaurants));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDto>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        RestaurantDto restaurant = restaurantService.updateRestaurant(id, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated", restaurant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        restaurantService.deleteRestaurant(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted", null));
    }
}

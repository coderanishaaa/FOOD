package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.ResourceNotFoundException;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Create a new restaurant. Only RESTAURANT_OWNER role can create.
     */
    @Transactional
    public RestaurantDto createRestaurant(RestaurantRequest request, Long ownerId, String role) {
        if (!"RESTAURANT_OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only restaurant owners can create restaurants");
        }

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .cuisine(request.getCuisine())
                .ownerId(ownerId)
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {} by owner {}", restaurant.getName(), ownerId);
        return mapToDto(restaurant);
    }

    /**
     * Get all active restaurants (public view for customers).
     */
    public List<RestaurantDto> getAllActiveRestaurants() {
        return restaurantRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get restaurant by ID.
     */
    public RestaurantDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return mapToDto(restaurant);
    }

    /**
     * Get restaurants owned by a specific user.
     */
    public List<RestaurantDto> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update restaurant — only the owner can update.
     */
    @Transactional
    public RestaurantDto updateRestaurant(Long id, RestaurantRequest request, Long userId, String role) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        // Only the owner or admin can update
        if (!restaurant.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to update this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setImageUrl(request.getImageUrl());

        restaurant = restaurantRepository.save(restaurant);
        return mapToDto(restaurant);
    }

    /**
     * Delete (deactivate) a restaurant.
     */
    @Transactional
    public void deleteRestaurant(Long id, Long userId, String role) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to delete this restaurant");
        }

        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
        log.info("Restaurant deactivated: {}", id);
    }

    private RestaurantDto mapToDto(Restaurant restaurant) {
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .cuisine(restaurant.getCuisine())
                .ownerId(restaurant.getOwnerId())
                .active(restaurant.isActive())
                .imageUrl(restaurant.getImageUrl())
                .menuItems(Collections.emptyList())  // Loaded separately via MenuItemService
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}

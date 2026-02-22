package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.ResourceNotFoundException;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public RestaurantDto createRestaurant(RestaurantRequest request, Long ownerId, String role) {
        if (!"RESTAURANT_OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only restaurant owners can create restaurants");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOwnerId(ownerId);
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setActive(true);

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {} by owner {}", restaurant.getName(), ownerId);
        return mapToDto(restaurant);
    }

    public List<RestaurantDto> getAllActiveRestaurants() {
        return restaurantRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public RestaurantDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return mapToDto(restaurant);
    }

    public List<RestaurantDto> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

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
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setCuisine(restaurant.getCuisine());
        dto.setOwnerId(restaurant.getOwnerId());
        dto.setActive(restaurant.isActive());
        dto.setImageUrl(restaurant.getImageUrl());
        dto.setMenuItems(Collections.emptyList()); // Loaded separately via MenuItemService
        dto.setCreatedAt(restaurant.getCreatedAt());
        return dto;
    }
}

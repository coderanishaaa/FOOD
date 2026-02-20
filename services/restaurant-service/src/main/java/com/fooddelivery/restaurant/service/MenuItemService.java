package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.MenuItemDto;
import com.fooddelivery.restaurant.dto.MenuItemRequest;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.ResourceNotFoundException;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Add a menu item to a restaurant. Only the restaurant owner can add items.
     */
    @Transactional
    public MenuItemDto addMenuItem(MenuItemRequest request, Long userId, String role) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        // Verify ownership
        if (!restaurant.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to add items to this restaurant");
        }

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .available(true)
                .restaurant(restaurant)
                .build();

        item = menuItemRepository.save(item);
        log.info("Menu item added: {} to restaurant {}", item.getName(), restaurant.getName());
        return mapToDto(item);
    }

    /**
     * Get all available menu items for a restaurant.
     */
    public List<MenuItemDto> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update a menu item.
     */
    @Transactional
    public MenuItemDto updateMenuItem(Long itemId, MenuItemRequest request, Long userId, String role) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getRestaurant().getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to update this item");
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());

        item = menuItemRepository.save(item);
        return mapToDto(item);
    }

    /**
     * Delete (set unavailable) a menu item.
     */
    @Transactional
    public void deleteMenuItem(Long itemId, Long userId, String role) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getRestaurant().getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to delete this item");
        }

        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    private MenuItemDto mapToDto(MenuItem item) {
        return MenuItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .restaurantId(item.getRestaurant().getId())
                .build();
    }
}

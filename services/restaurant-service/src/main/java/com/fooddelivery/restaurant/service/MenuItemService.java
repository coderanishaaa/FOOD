package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.MenuItemDto;
import com.fooddelivery.restaurant.dto.MenuItemRequest;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.ResourceNotFoundException;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private static final Logger log = LoggerFactory.getLogger(MenuItemService.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public MenuItemDto addMenuItem(MenuItemRequest request, Long userId, String role) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not authorized to add items to this restaurant");
        }

        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());
        item.setAvailable(true);
        item.setRestaurant(restaurant);

        item = menuItemRepository.save(item);
        log.info("Menu item added: {} to restaurant {}", item.getName(), restaurant.getName());
        return mapToDto(item);
    }

    public List<MenuItemDto> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

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
        MenuItemDto dto = new MenuItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCategory(item.getCategory());
        dto.setImageUrl(item.getImageUrl());
        dto.setAvailable(item.isAvailable());
        dto.setRestaurantId(item.getRestaurant().getId());
        return dto;
    }
}

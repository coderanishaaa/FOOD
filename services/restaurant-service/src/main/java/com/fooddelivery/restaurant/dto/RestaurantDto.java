package com.fooddelivery.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String cuisine;
    private Long ownerId;
    private boolean active;
    private String imageUrl;
    private List<MenuItemDto> menuItems;
    private LocalDateTime createdAt;
}

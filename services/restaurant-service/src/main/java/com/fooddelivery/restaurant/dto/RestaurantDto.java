package com.fooddelivery.restaurant.dto;

import java.time.LocalDateTime;
import java.util.List;

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

    public RestaurantDto() {
    }

    public RestaurantDto(Long id, String name, String address, String phone, String cuisine, Long ownerId,
            boolean active, String imageUrl, List<MenuItemDto> menuItems, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cuisine = cuisine;
        this.ownerId = ownerId;
        this.active = active;
        this.imageUrl = imageUrl;
        this.menuItems = menuItems;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<MenuItemDto> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDto> menuItems) {
        this.menuItems = menuItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

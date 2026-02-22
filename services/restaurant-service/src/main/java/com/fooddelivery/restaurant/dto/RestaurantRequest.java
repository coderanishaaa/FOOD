package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;

    @NotBlank(message = "Cuisine type is required")
    private String cuisine;

    private String imageUrl;

    public RestaurantRequest() {
    }

    public RestaurantRequest(String name, String address, String phone, String cuisine, String imageUrl) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cuisine = cuisine;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

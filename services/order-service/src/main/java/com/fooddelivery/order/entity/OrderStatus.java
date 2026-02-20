package com.fooddelivery.order.entity;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PAYMENT_PENDING,
    PAID,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

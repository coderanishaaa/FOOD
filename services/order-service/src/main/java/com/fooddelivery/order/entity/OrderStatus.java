package com.fooddelivery.order.entity;

public enum OrderStatus {
    PENDING_PAYMENT,
    PLACED,
    CONFIRMED,
    PAID,
    ASSIGNED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

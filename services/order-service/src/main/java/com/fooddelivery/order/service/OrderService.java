package com.fooddelivery.order.service;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.event.OrderEvent;
import com.fooddelivery.order.event.OrderEventProducer;
import com.fooddelivery.order.exception.ResourceNotFoundException;
import com.fooddelivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * Place a new order:
     * 1. Calculate total from items
     * 2. Save order to DB
     * 3. Publish OrderEvent to Kafka for payment processing
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, Long customerId) {
        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build order entity
        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .status(OrderStatus.PLACED)
                .build();

        // Build order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .menuItemId(itemReq.getMenuItemId())
                        .menuItemName(itemReq.getMenuItemName())
                        .quantity(itemReq.getQuantity())
                        .price(itemReq.getPrice())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed: orderId={}, customerId={}, total={}", savedOrder.getId(), customerId, totalAmount);

        // Publish event to Kafka for payment-service to consume
        OrderEvent event = OrderEvent.builder()
                .orderId(savedOrder.getId())
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .totalAmount(totalAmount)
                .status(OrderStatus.PLACED.name())
                .deliveryAddress(request.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventProducer.publishOrderEvent(event);

        return mapToResponse(savedOrder);
    }

    /**
     * Get order by ID.
     */
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    /**
     * Get all orders for a customer.
     */
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders for a restaurant.
     */
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update order status (called internally or by consuming Kafka events).
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.setStatus(OrderStatus.valueOf(status));
        order = orderRepository.save(order);
        log.info("Order status updated: orderId={}, status={}", orderId, status);

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

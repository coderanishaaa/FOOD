package com.fooddelivery.order.service;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.event.OrderEvent;
import com.fooddelivery.order.event.OrderEventProducer;
import com.fooddelivery.order.exception.ResourceNotFoundException;
import com.fooddelivery.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

        private static final Logger log = LoggerFactory.getLogger(OrderService.class);

        private final OrderRepository orderRepository;
        private final OrderEventProducer orderEventProducer;
        private final JdbcTemplate jdbcTemplate;

        public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer,
                        JdbcTemplate jdbcTemplate) {
                this.orderRepository = orderRepository;
                this.orderEventProducer = orderEventProducer;
                this.jdbcTemplate = jdbcTemplate;
        }

        @PostConstruct
        public void init() {
                try {
                        jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN status VARCHAR(255)");
                        log.info("Successfully altered table orders modifying status column to VARCHAR(255)");
                } catch (Exception e) {
                        log.warn("Could not alter table orders status column: {}", e.getMessage());
                }
        }

        @Transactional
        public OrderResponse placeOrder(OrderRequest request, Long customerId) {
                BigDecimal totalAmount = request.getItems().stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Order order = new Order();
                order.setCustomerId(customerId);
                order.setRestaurantId(request.getRestaurantId());
                order.setTotalAmount(totalAmount);
                order.setDeliveryAddress(request.getDeliveryAddress());
                order.setStatus(OrderStatus.PENDING_RESTAURANT_CONFIRMATION);

                List<OrderItem> orderItems = request.getItems().stream()
                                .map(itemReq -> {
                                        OrderItem item = new OrderItem();
                                        item.setMenuItemId(itemReq.getMenuItemId());
                                        item.setMenuItemName(itemReq.getMenuItemName());
                                        item.setQuantity(itemReq.getQuantity());
                                        item.setPrice(itemReq.getPrice());
                                        item.setOrder(order);
                                        return item;
                                })
                                .collect(Collectors.toList());

                order.setItems(orderItems);
                Order savedOrder = orderRepository.save(order);
                log.info("Order placed: orderId={}, customerId={}, total={}", savedOrder.getId(), customerId,
                                totalAmount);

                OrderEvent event = new OrderEvent();
                event.setOrderId(savedOrder.getId());
                event.setCustomerId(customerId);
                event.setRestaurantId(request.getRestaurantId());
                event.setTotalAmount(totalAmount);
                event.setStatus(OrderStatus.PENDING_RESTAURANT_CONFIRMATION.name());
                event.setDeliveryAddress(request.getDeliveryAddress());
                event.setTimestamp(LocalDateTime.now());

                orderEventProducer.publishOrderEvent(event);

                return mapToResponse(savedOrder);
        }

        public OrderResponse getOrderById(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
                return mapToResponse(order);
        }

        public List<OrderResponse> getOrdersByCustomer(Long customerId) {
                return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
                return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

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
                                .map(item -> {
                                        OrderItemResponse res = new OrderItemResponse();
                                        res.setId(item.getId());
                                        res.setMenuItemId(item.getMenuItemId());
                                        res.setMenuItemName(item.getMenuItemName());
                                        res.setQuantity(item.getQuantity());
                                        res.setPrice(item.getPrice());
                                        return res;
                                })
                                .collect(Collectors.toList());

                OrderResponse res = new OrderResponse();
                res.setId(order.getId());
                res.setCustomerId(order.getCustomerId());
                res.setRestaurantId(order.getRestaurantId());
                res.setStatus(order.getStatus().name());
                res.setTotalAmount(order.getTotalAmount());
                res.setDeliveryAddress(order.getDeliveryAddress());
                res.setItems(items);
                res.setCreatedAt(order.getCreatedAt());
                res.setUpdatedAt(order.getUpdatedAt());
                return res;
        }
}

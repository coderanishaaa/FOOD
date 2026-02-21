package com.fooddelivery.payment.service;

import com.fooddelivery.payment.client.OrderServiceClient;
import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import com.fooddelivery.payment.dto.PaymentDto;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.entity.PaymentStatus;
import com.fooddelivery.payment.event.OrderEvent;
import com.fooddelivery.payment.event.PaymentEvent;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment processing service with Stripe integration.
 * 
 * Flow:
 * 1. Order created → OrderEvent published → Payment record created with PENDING
 * status
 * 2. Customer clicks payment button → Stripe Checkout Session created
 * 3. Customer completes payment on Stripe → Webhook received
 * 4. Payment status updated to COMPLETED → Order status updated to PAID →
 * PaymentEvent published
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final OrderServiceClient orderServiceClient;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    /**
     * Create or get payment record for an order event.
     * This is called when order is placed - payment is not processed yet.
     */
    @Transactional
    public PaymentDto createPaymentRecord(OrderEvent orderEvent) {
        log.info("Creating payment record for orderId={}", orderEvent.getOrderId());

        // Check if payment already exists
        Payment existingPayment = paymentRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);
        if (existingPayment != null) {
            log.info("Payment record already exists for orderId={}", orderEvent.getOrderId());
            return mapToDto(existingPayment);
        }

        // Create payment record with PENDING status
        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .customerId(orderEvent.getCustomerId())
                .amount(orderEvent.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        return mapToDto(payment);
    }

    /**
     * Handle successful Stripe payment via webhook.
     * Updates payment status and order status, then publishes PaymentEvent.
     */
    @Transactional
    public PaymentDto handleSuccessfulPayment(String stripeSessionId, String stripePaymentIntentId) {
        log.info("Handling successful payment: sessionId={}, paymentIntentId={}", stripeSessionId,
                stripePaymentIntentId);

        // Find payment by Stripe session ID
        Payment payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for Stripe session: " + stripeSessionId));

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setStripePaymentIntentId(stripePaymentIntentId);
        payment.setTransactionId(stripePaymentIntentId);
        payment = paymentRepository.save(payment);
        log.info("Payment completed: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        // Update order status to PAID via order-service
        try {
            ApiResponse<OrderResponse> orderResponse = orderServiceClient.updateOrderStatus(
                    payment.getOrderId(),
                    "PAID");
            log.info("Order status updated to PAID: orderId={}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order status for orderId={}: {}", payment.getOrderId(), e.getMessage());
            // Continue even if order update fails - we can retry later
        }

        // Fetch order details for event
        OrderResponse order = null;
        try {
            ApiResponse<OrderResponse> orderResponse = orderServiceClient.getOrderById(payment.getOrderId());
            order = orderResponse != null ? orderResponse.getData() : null;
        } catch (Exception e) {
            log.error("Failed to fetch order details: {}", e.getMessage());
        }

        // Publish payment success event for delivery-service
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .deliveryAddress(order != null ? order.getDeliveryAddress() : null)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(payment.getOrderId()), paymentEvent);
        log.info("Published payment success event for orderId={}", payment.getOrderId());

        return mapToDto(payment);
    }

    @Transactional
    public PaymentDto handleSuccessfulPaymentByOrderId(Long orderId, String transactionId) {
        log.info("Handling successful payment by orderId: {}, transactionId={}", orderId, transactionId);

        // Find payment by order ID directly instead of using Razorpay Order ID as
        // Stripe Session ID
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        payment = paymentRepository.save(payment);
        log.info("Payment completed: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        // Update order status to PAID via order-service
        try {
            ApiResponse<OrderResponse> orderResponse = orderServiceClient.updateOrderStatus(
                    payment.getOrderId(),
                    "PAID");
            log.info("Order status updated to PAID: orderId={}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order status for orderId={}: {}", payment.getOrderId(), e.getMessage());
            // Continue even if order update fails - we can retry later
        }

        // Fetch order details for event
        OrderResponse order = null;
        try {
            ApiResponse<OrderResponse> orderResponse = orderServiceClient.getOrderById(payment.getOrderId());
            order = orderResponse != null ? orderResponse.getData() : null;
        } catch (Exception e) {
            log.error("Failed to fetch order details: {}", e.getMessage());
        }

        // Publish payment success event for delivery-service
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .deliveryAddress(order != null ? order.getDeliveryAddress() : null)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(payment.getOrderId()), paymentEvent);
        log.info("Published payment success event for orderId={}", payment.getOrderId());

        return mapToDto(payment);
    }

    /**
     * Save Stripe session ID to payment record when checkout session is created.
     * Creates payment record if it doesn't exist (for backward compatibility with
     * PLACED orders).
     */
    @Transactional
    public void saveStripeSessionId(Long orderId, String stripeSessionId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            // Payment record doesn't exist - create it (for backward compatibility with
            // PLACED orders)
            log.info("Payment record not found for orderId={}, fetching order details to create one", orderId);

            try {
                // Fetch order details to get customerId and amount
                ApiResponse<OrderResponse> orderResponse = orderServiceClient.getOrderById(orderId);
                if (orderResponse == null || orderResponse.getData() == null) {
                    throw new RuntimeException("Order not found: " + orderId);
                }

                OrderResponse order = orderResponse.getData();
                payment = Payment.builder()
                        .orderId(orderId)
                        .customerId(order.getCustomerId())
                        .amount(order.getTotalAmount())
                        .status(PaymentStatus.PENDING)
                        .stripeSessionId(stripeSessionId)
                        .build();
                log.info("Created payment record for orderId={}", orderId);
            } catch (Exception e) {
                log.error("Failed to create payment record for orderId={}: {}", orderId, e.getMessage());
                throw new RuntimeException("Failed to create payment record: " + e.getMessage(), e);
            }
        } else {
            payment.setStripeSessionId(stripeSessionId);
        }

        payment = paymentRepository.save(payment);
        log.info("Stripe session ID saved: orderId={}, sessionId={}", orderId, stripeSessionId);
    }

    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            // Return a default payment DTO with PENDING status instead of throwing
            // exception
            log.info("Payment not found for orderId={}, returning default PENDING status", orderId);
            return PaymentDto.builder()
                    .orderId(orderId)
                    .status("PENDING")
                    .build();
        }

        return mapToDto(payment);
    }

    /**
     * Get payment entity by order ID (for internal use).
     */
    public Payment getPaymentEntityByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    private PaymentDto mapToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

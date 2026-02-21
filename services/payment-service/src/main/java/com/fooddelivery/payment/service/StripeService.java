package com.fooddelivery.payment.service;

import com.fooddelivery.payment.client.OrderServiceClient;
import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.CheckoutSessionResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Stripe payment operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final OrderServiceClient orderServiceClient;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    /**
     * Create a Stripe Checkout Session for an order.
     * 
     * @param orderId The order ID
     * @return CheckoutSessionResponse with session URL
     */
    public CheckoutSessionResponse createCheckoutSession(Long orderId) throws StripeException {
        log.info("Creating Stripe checkout session for orderId={}", orderId);

        // Fetch order details from order-service with error handling
        ApiResponse<OrderResponse> orderResponse;
        try {
            orderResponse = orderServiceClient.getOrderById(orderId);
        } catch (Exception e) {
            log.error("Failed to fetch order from order-service for orderId={}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to fetch order details: " + e.getMessage() + 
                ". Ensure order-service is running and order exists.", e);
        }
        
        if (orderResponse == null || orderResponse.getData() == null) {
            log.error("Order not found: orderId={}", orderId);
            throw new RuntimeException("Order not found: " + orderId);
        }

        OrderResponse order = orderResponse.getData();
        
        // Verify order is in PENDING_PAYMENT or PLACED status (PLACED for backward compatibility)
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PENDING_PAYMENT or PLACED status. Current status: " + order.getStatus());
        }

        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // Build line items for Stripe Checkout
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        
        // Add order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            order.getItems().forEach(item -> {
                long itemPriceInCents = item.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
                lineItems.add(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(Long.valueOf(item.getQuantity()))
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(itemPriceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getMenuItemName())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );
            });
        } else {
            // Fallback: single line item for total amount
            lineItems.add(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Order #" + orderId)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        }

        // Create Stripe Checkout Session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&order_id=" + orderId)
                .setCancelUrl(cancelUrl + "?order_id=" + orderId)
                .addAllLineItem(lineItems)
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("customerId", String.valueOf(order.getCustomerId()))
                .build();

        Session session = Session.create(params);
        log.info("Stripe checkout session created: sessionId={}, orderId={}", session.getId(), orderId);

        return CheckoutSessionResponse.builder()
                .sessionId(session.getId())
                .url(session.getUrl())
                .build();
    }

    /**
     * Retrieve a Stripe Checkout Session by session ID.
     */
    public Session getCheckoutSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    /**
     * Create a Stripe Embedded Checkout Session for an order.
     * Returns clientSecret instead of redirect URL for embedded form integration.
     * 
     * @param orderId The order ID
     * @return CheckoutSessionResponse with client secret for embedded checkout
     */
    public CheckoutSessionResponse createEmbeddedCheckoutSession(Long orderId) throws StripeException {
        log.info("Creating Stripe embedded checkout session for orderId={}", orderId);

        // Fetch order details from order-service with error handling
        ApiResponse<OrderResponse> orderResponse;
        try {
            orderResponse = orderServiceClient.getOrderById(orderId);
        } catch (Exception e) {
            log.error("Failed to fetch order from order-service for orderId={}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to fetch order details: " + e.getMessage() + 
                ". Ensure order-service is running and order exists.", e);
        }
        
        if (orderResponse == null || orderResponse.getData() == null) {
            log.error("Order not found: orderId={}", orderId);
            throw new RuntimeException("Order not found: " + orderId);
        }

        OrderResponse order = orderResponse.getData();
        
        // Verify order is in PENDING_PAYMENT or PLACED status
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PENDING_PAYMENT or PLACED status. Current status: " + order.getStatus());
        }

        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // Build line items for Stripe Checkout
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        
        // Add order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            order.getItems().forEach(item -> {
                long itemPriceInCents = item.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
                lineItems.add(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(Long.valueOf(item.getQuantity()))
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(itemPriceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getMenuItemName())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );
            });
        } else {
            // Fallback: single line item for total amount
            lineItems.add(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Order #" + orderId)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        }

        // Create Stripe Checkout Session with embedded mode (ui_mode: embedded)
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)  // Enable embedded checkout
                .setReturnUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&order_id=" + orderId)
                .addAllLineItem(lineItems)
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("customerId", String.valueOf(order.getCustomerId()))
                .build();

        Session session = Session.create(params);
        log.info("Stripe embedded checkout session created: sessionId={}, orderId={}, clientSecret={}", 
                session.getId(), orderId, session.getClientSecret());

        return CheckoutSessionResponse.builder()
                .sessionId(session.getId())
                .clientSecret(session.getClientSecret())  // Return client secret instead of URL
                .build();
    }

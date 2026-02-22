package com.fooddelivery.payment.service;

import com.fooddelivery.payment.client.OrderServiceClient;
import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.CheckoutSessionResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final OrderServiceClient orderServiceClient;

    public StripeService(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(Long orderId) throws StripeException {
        log.info("Creating Stripe checkout session for orderId={}", orderId);

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

        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            throw new RuntimeException(
                    "Order is not in PENDING_PAYMENT or PLACED status. Current status: " + order.getStatus());
        }

        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

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
                                                                .build())
                                                .build())
                                .build());
            });
        } else {
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
                                                            .build())
                                            .build())
                            .build());
        }

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

        CheckoutSessionResponse response = new CheckoutSessionResponse();
        response.setSessionId(session.getId());
        response.setUrl(session.getUrl());
        return response;
    }

    public Session getCheckoutSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    public CheckoutSessionResponse createEmbeddedCheckoutSession(Long orderId) throws StripeException {
        log.info("Creating Stripe embedded checkout session for orderId={}", orderId);

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

        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            throw new RuntimeException(
                    "Order is not in PENDING_PAYMENT or PLACED status. Current status: " + order.getStatus());
        }

        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

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
                                                                .build())
                                                .build())
                                .build());
            });
        } else {
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
                                                            .build())
                                            .build())
                            .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setReturnUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&order_id=" + orderId)
                .addAllLineItem(lineItems)
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("customerId", String.valueOf(order.getCustomerId()))
                .build();

        Session session = Session.create(params);
        log.info("Stripe embedded checkout session created: sessionId={}, orderId={}, clientSecret={}",
                session.getId(), orderId, session.getClientSecret());

        CheckoutSessionResponse response = new CheckoutSessionResponse();
        response.setSessionId(session.getId());
        response.setClientSecret(session.getClientSecret());
        return response;
    }
}
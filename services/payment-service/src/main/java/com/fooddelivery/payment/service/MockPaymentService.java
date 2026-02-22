package com.fooddelivery.payment.service;

import com.fooddelivery.payment.client.OrderServiceClient;
import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.CheckoutSessionResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "stripe.mock-mode", havingValue = "true", matchIfMissing = false)
public class MockPaymentService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentService.class);

    private final OrderServiceClient orderServiceClient;

    public MockPaymentService(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @Value("${stripe.success-url:http://localhost:3000/payment/success}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:3000/customer/orders}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(Long orderId) {
        log.info("MOCK MODE: Creating mock checkout session for orderId={}", orderId);

        ApiResponse<OrderResponse> orderResponse;
        try {
            log.info("Fetching order details from order-service for orderId={}", orderId);
            orderResponse = orderServiceClient.getOrderById(orderId);
            log.info("Order service response received: {}", orderResponse != null ? "not null" : "null");
        } catch (Exception e) {
            log.error("❌ Failed to fetch order from order-service: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot reach order-service. Error: " + e.getMessage() +
                    ". Ensure order-service is running and registered in Eureka.", e);
        }

        if (orderResponse == null || orderResponse.getData() == null) {
            log.error("Order not found: orderId={}", orderId);
            throw new RuntimeException("Order not found: " + orderId + ". Verify the order exists in order-service.");
        }

        OrderResponse order = orderResponse.getData();
        log.info("Order found: orderId={}, status={}, totalAmount={}", orderId, order.getStatus(),
                order.getTotalAmount());

        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            log.warn("Order status is not PENDING_PAYMENT or PLACED: orderId={}, status={}", orderId,
                    order.getStatus());
            throw new RuntimeException(
                    "Order is not in PENDING_PAYMENT or PLACED status. Current status: " + order.getStatus());
        }

        String mockSessionId = "mock_session_" + UUID.randomUUID().toString().substring(0, 8);

        String mockCheckoutUrl = successUrl + "?session_id=" + mockSessionId + "&order_id=" + orderId + "&mock=true";

        log.info("MOCK MODE: Created mock checkout session: sessionId={}, url={}", mockSessionId, mockCheckoutUrl);

        CheckoutSessionResponse response = new CheckoutSessionResponse();
        response.setSessionId(mockSessionId);
        response.setUrl(mockCheckoutUrl);
        return response;
    }
}

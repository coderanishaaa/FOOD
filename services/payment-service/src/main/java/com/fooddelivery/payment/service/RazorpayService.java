package com.fooddelivery.payment.service;

import com.fooddelivery.payment.client.OrderServiceClient;
import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.OrderResponse;
import com.fooddelivery.payment.dto.RazorpayOrderResponse;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {

    private final OrderServiceClient orderServiceClient;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.test-mode:true}")
    private boolean testMode;

    public RazorpayOrderResponse createRazorpayOrder(Long internalOrderId) throws RazorpayException {
        log.info("Creating Razorpay order for internalOrderId={}", internalOrderId);

        // Fetch order details from order-service
        ApiResponse<OrderResponse> orderResponse;
        try {
            orderResponse = orderServiceClient.getOrderById(internalOrderId);
        } catch (Exception e) {
            log.error("Failed to fetch order from order-service for orderId={}: {}", internalOrderId, e.getMessage());
            throw new RuntimeException("Failed to fetch order details. Ensure order-service is running.", e);
        }

        if (orderResponse == null || orderResponse.getData() == null) {
            throw new RuntimeException("Order not found: " + internalOrderId);
        }

        OrderResponse order = orderResponse.getData();

        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PLACED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PENDING_PAYMENT or PLACED status.");
        }

        if (razorpayKeyId == null || razorpayKeyId.isEmpty() || razorpayKeySecret == null
                || razorpayKeySecret.isEmpty()) {
            throw new RuntimeException(
                    "Razorpay API keys are not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in your environment variables.");
        }

        long amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt_" + internalOrderId);

        Order razorpayOrder = razorpay.orders.create(orderRequest);

        log.info("Razorpay order created: id={}, internalOrderId={}", razorpayOrder.get("id"), internalOrderId);

        return RazorpayOrderResponse.builder()
                .orderId(razorpayOrder.get("id"))
                .amount(order.getTotalAmount())
                .currency("INR")
                .keyId(razorpayKeyId)
                .customerName("Customer " + order.getCustomerId())
                .customerEmail("customer" + order.getCustomerId() + "@example.com")
                .build();
    }
}

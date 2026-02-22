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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final OrderServiceClient orderServiceClient;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, PaymentEvent> kafkaTemplate,
            OrderServiceClient orderServiceClient) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderServiceClient = orderServiceClient;
    }

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @Transactional
    public PaymentDto createPaymentRecord(OrderEvent orderEvent) {
        log.info("Creating payment record for orderId={}", orderEvent.getOrderId());

        Payment existingPayment = paymentRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);
        if (existingPayment != null) {
            log.info("Payment record already exists for orderId={}", orderEvent.getOrderId());
            return mapToDto(existingPayment);
        }

        Payment payment = new Payment();
        payment.setOrderId(orderEvent.getOrderId());
        payment.setCustomerId(orderEvent.getCustomerId());
        payment.setAmount(orderEvent.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);
        log.info("Payment record created: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        return mapToDto(payment);
    }

    @Transactional
    public PaymentDto handleSuccessfulPaymentByOrderId(Long orderId, String transactionId) {
        log.info("Handling successful payment by orderId: {}, transactionId={}", orderId, transactionId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

        payment.setStatus(PaymentStatus.COMPLETED);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        payment = paymentRepository.save(payment);
        log.info("Payment completed: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        try {
            orderServiceClient.updateOrderStatus(
                    payment.getOrderId(),
                    "PAID");
            log.info("Order status updated to PAID: orderId={}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order status for orderId={}: {}", payment.getOrderId(), e.getMessage());
        }

        OrderResponse order = null;
        try {
            ApiResponse<OrderResponse> orderResponse = orderServiceClient.getOrderById(payment.getOrderId());
            order = orderResponse != null ? orderResponse.getData() : null;
        } catch (Exception e) {
            log.error("Failed to fetch order details: {}", e.getMessage());
        }

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setPaymentId(payment.getId());
        paymentEvent.setOrderId(payment.getOrderId());
        paymentEvent.setCustomerId(payment.getCustomerId());
        paymentEvent.setAmount(payment.getAmount());
        paymentEvent.setStatus(payment.getStatus().name());
        paymentEvent.setDeliveryAddress(order != null ? order.getDeliveryAddress() : null);
        paymentEvent.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(payment.getOrderId()), paymentEvent);
        log.info("Published payment success event for orderId={}", payment.getOrderId());

        return mapToDto(payment);
    }

    @Transactional
    public void savePaymentSessionId(Long orderId, String paymentSessionId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            log.info("Payment record not found for orderId={}, fetching order details to create one", orderId);

            try {
                ApiResponse<OrderResponse> orderResponse = orderServiceClient.getOrderById(orderId);
                if (orderResponse == null || orderResponse.getData() == null) {
                    throw new RuntimeException("Order not found: " + orderId);
                }

                OrderResponse order = orderResponse.getData();
                payment = new Payment();
                payment.setOrderId(orderId);
                payment.setCustomerId(order.getCustomerId());
                payment.setAmount(order.getTotalAmount());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setPaymentSessionId(paymentSessionId);
                log.info("Created payment record for orderId={}", orderId);
            } catch (Exception e) {
                log.error("Failed to create payment record for orderId={}: {}", orderId, e.getMessage());
                throw new RuntimeException("Failed to create payment record: " + e.getMessage(), e);
            }
        } else {
            payment.setPaymentSessionId(paymentSessionId);
        }

        payment = paymentRepository.save(payment);
        log.info("Payment session ID saved: orderId={}, sessionId={}", orderId, paymentSessionId);
    }

    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            log.info("Payment not found for orderId={}, returning default PENDING status", orderId);
            PaymentDto defaultDto = new PaymentDto();
            defaultDto.setOrderId(orderId);
            defaultDto.setStatus("PENDING");
            return defaultDto;
        }

        return mapToDto(payment);
    }

    public Payment getPaymentEntityByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    private PaymentDto mapToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}

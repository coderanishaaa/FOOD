package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.PaymentDto;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.entity.PaymentStatus;
import com.fooddelivery.payment.event.OrderEvent;
import com.fooddelivery.payment.event.PaymentEvent;
import com.fooddelivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simulated payment processing service.
 * 
 * When an OrderEvent is received:
 * 1. Creates a Payment record with PENDING status
 * 2. Simulates payment processing (always succeeds in this demo)
 * 3. Updates status to COMPLETED
 * 4. Publishes PaymentEvent to Kafka for delivery-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    /**
     * Process payment for an incoming order event.
     */
    @Transactional
    public PaymentDto processPayment(OrderEvent orderEvent) {
        log.info("Processing payment for orderId={}", orderEvent.getOrderId());

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .customerId(orderEvent.getCustomerId())
                .amount(orderEvent.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())  // Simulated transaction ID
                .build();

        payment = paymentRepository.save(payment);

        // Simulate payment processing (always succeeds for demo)
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);
        log.info("Payment completed: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());

        // Publish payment event for delivery-service
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .deliveryAddress(orderEvent.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(payment.getOrderId()), paymentEvent);
        log.info("Published payment event for orderId={}", payment.getOrderId());

        return mapToDto(payment);
    }

    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return mapToDto(payment);
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

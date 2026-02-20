package com.fooddelivery.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published to "payment-events" topic after payment is processed.
 * Consumed by delivery-service and notification-service.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status;          // COMPLETED or FAILED
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

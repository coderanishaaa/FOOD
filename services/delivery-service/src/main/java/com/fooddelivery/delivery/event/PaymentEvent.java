package com.fooddelivery.delivery.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status;
    private String deliveryAddress;
    private LocalDateTime timestamp;
}

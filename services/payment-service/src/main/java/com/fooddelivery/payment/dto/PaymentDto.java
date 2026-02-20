package com.fooddelivery.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDto {
    private Long id;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private LocalDateTime createdAt;
}

package com.fooddelivery.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
    private String orderId; // Razorpay order_id
    private BigDecimal amount;
    private String currency;
    private String keyId; // Razorpay Key ID
    private String customerName;
    private String customerEmail;
}

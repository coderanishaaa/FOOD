package com.fooddelivery.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionResponse {
    private String sessionId;
    private String url;  // Stripe Checkout URL (for hosted mode)
    private String clientSecret;  // Client secret for embedded checkout
}

package com.fooddelivery.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "stripe.mock-mode", havingValue = "false", matchIfMissing = false)
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty() ||
                secretKey.contains("YourStripeSecretKeyHere") ||
                secretKey.equals("sk_test_51YourStripeSecretKeyHere")) {
            log.warn("⚠️  Stripe secret key not configured or using placeholder value!");
            log.warn("⚠️  Set STRIPE_SECRET_KEY environment variable or enable mock mode (stripe.mock-mode=true)");
            throw new IllegalStateException(
                    "Stripe API key not configured. " +
                            "Set STRIPE_SECRET_KEY environment variable or enable mock mode by setting stripe.mock-mode=true");
        }

        try {
            Stripe.apiKey = secretKey;
            log.info("✅ Stripe API key initialized successfully");
            log.info("✅ Stripe API version: {}", Stripe.API_VERSION);
        } catch (Exception e) {
            log.error("❌ Failed to initialize Stripe API key: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize Stripe: " + e.getMessage(), e);
        }
    }
}

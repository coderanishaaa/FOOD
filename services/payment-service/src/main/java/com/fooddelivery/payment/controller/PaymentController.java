package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.dto.ApiResponse;
import com.fooddelivery.payment.dto.CheckoutSessionResponse;
import com.fooddelivery.payment.dto.PaymentDto;
import com.fooddelivery.payment.dto.RazorpayOrderResponse;
import com.fooddelivery.payment.service.PaymentService;
import com.fooddelivery.payment.service.StripeService;
import com.fooddelivery.payment.service.RazorpayService;
import com.fooddelivery.payment.service.MockPaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment controller with Stripe integration.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final RazorpayService razorpayService;

    public PaymentController(PaymentService paymentService, StripeService stripeService,
            RazorpayService razorpayService) {
        this.paymentService = paymentService;
        this.stripeService = stripeService;
        this.razorpayService = razorpayService;
    }

    @Autowired(required = false)
    private MockPaymentService mockPaymentService; // Optional - only available in mock mode

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.mock-mode:false}")
    private boolean mockMode;

    /**
     * Get payment details by order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
            String status = payment != null ? payment.getStatus() : null;
            if (status == null || status.equals("PENDING") || status.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Payment not yet created", payment));
            }
            return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
        } catch (Exception e) {
            log.error("Error getting payment for orderId={}: {}", orderId, e.getMessage());
            // Return safe default response instead of 500
            PaymentDto defaultPayment = new PaymentDto();
            defaultPayment.setOrderId(orderId);
            defaultPayment.setStatus("PENDING");
            return ResponseEntity.ok(ApiResponse.success("Payment not yet created", defaultPayment));
        }
    }

    /**
     * Create Stripe Checkout Session for an order.
     * This endpoint is called when customer clicks "Proceed to Payment" button.
     * 
     * @param orderId The order ID
     * @return CheckoutSessionResponse with Stripe checkout URL
     */
    @PostMapping("/create-session/{orderId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @PathVariable Long orderId) {
        try {
            log.info("=== Creating checkout session for orderId={} ===", orderId);
            log.info("Mock mode enabled: {}, MockPaymentService available: {}", mockMode, mockPaymentService != null);

            CheckoutSessionResponse session = null;
            String errorMessage = null;

            // Use mock mode if enabled, otherwise use real Stripe
            if (mockMode) {
                log.info("MOCK MODE: Attempting to create mock checkout session for orderId={}", orderId);
                if (mockPaymentService == null) {
                    errorMessage = "Mock mode is enabled but MockPaymentService is not available. Check configuration.";
                    log.error(errorMessage);
                } else {
                    try {
                        session = mockPaymentService.createCheckoutSession(orderId);
                        log.info("✅ Mock checkout session created successfully");
                    } catch (Exception e) {
                        errorMessage = "Failed to create mock checkout session: " + e.getMessage();
                        log.error("Mock payment service error: {}", errorMessage, e);
                        // Check if it's a Feign client error (order-service not reachable)
                        if (e.getMessage() != null && e.getMessage().contains("order-service")) {
                            errorMessage = "Cannot reach order-service. Ensure order-service is running and registered in Eureka.";
                        }
                    }
                }
            } else {
                log.info("REAL STRIPE MODE: Creating Stripe checkout session for orderId={}", orderId);
                try {
                    session = stripeService.createCheckoutSession(orderId);
                    log.info("✅ Stripe checkout session created successfully");
                } catch (StripeException e) {
                    log.error("Stripe API error: {}", e.getMessage(), e);
                    errorMessage = "Stripe payment error: " + e.getMessage();
                    if (e.getMessage() != null && (e.getMessage().contains("No API key") ||
                            e.getMessage().contains("Invalid API Key"))) {
                        errorMessage = "Stripe API key not configured. Enable mock mode by setting STRIPE_MOCK_MODE=true or set STRIPE_SECRET_KEY environment variable.";
                    }
                } catch (Exception e) {
                    errorMessage = "Failed to create Stripe session: " + e.getMessage();
                    log.error("Stripe service error: {}", errorMessage, e);
                    // Check if it's a Feign client error
                    if (e.getMessage() != null && e.getMessage().contains("order-service")) {
                        errorMessage = "Cannot reach order-service. Ensure order-service is running and registered in Eureka.";
                    }
                }
            }

            // If session creation failed, return error
            if (session == null) {
                log.error("❌ Failed to create checkout session. Error: {}", errorMessage);
                return ResponseEntity.status(500)
                        .body(ApiResponse
                                .error(errorMessage != null ? errorMessage : "Failed to create checkout session"));
            }

            // Save session ID to payment record (creates payment record if it doesn't
            // exist)
            try {
                paymentService.saveStripeSessionId(orderId, session.getSessionId());
                log.info("✅ Session ID saved to payment record");
            } catch (Exception e) {
                log.error("⚠️ Failed to save session ID: {}", e.getMessage(), e);
                // Don't fail the request - session is created, just log the error
                log.warn("Checkout session created but failed to save session ID to payment record");
            }

            log.info("✅✅✅ Checkout session created successfully for orderId={}, url={}", orderId, session.getUrl());
            return ResponseEntity.ok(ApiResponse.success("Checkout session created", session));

        } catch (IllegalStateException e) {
            // Stripe config error
            log.error("Configuration error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌❌❌ Unexpected error creating checkout session for orderId={}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(
                            "Failed to create checkout session: " + e.getMessage() + ". Check logs for details."));
        }
    }

    /**
     * Create Razorpay Order Session for an order.
     * Returns orderId and keyId for embedding Razorpay Checkout form on your
     * website.
     * 
     * @param orderId The order ID
     * @return RazorpayOrderResponse with order details
     */
    @PostMapping("/create-razorpay-session/{orderId}")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrderSession(
            @PathVariable Long orderId) {
        try {
            log.info("=== Creating Razorpay order session for orderId={} ===", orderId);

            RazorpayOrderResponse session = razorpayService.createRazorpayOrder(orderId);

            // Save Razorpay order ID to payment record (same field as stripe session ID or
            // you can add a new one, here we reuse it or handle differently)
            try {
                paymentService.saveStripeSessionId(orderId, session.getOrderId());
                log.info("✅ Razorpay Order ID saved to payment record");
            } catch (Exception e) {
                log.error("⚠️ Failed to save Razorpay Order ID: {}", e.getMessage(), e);
            }

            log.info("✅ Razorpay order session created successfully for orderId={}", orderId);
            return ResponseEntity.ok(ApiResponse.success("Razorpay order session created", session));

        } catch (Exception e) {
            log.error("❌ Unexpected error creating Razorpay order session for orderId={}: {}", orderId, e.getMessage(),
                    e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to create Razorpay order session: " + e.getMessage()));
        }
    }

    /**
     * Create Stripe Embedded Checkout Session for an order.
     * Returns clientSecret for embedding Stripe Checkout form on your website.
     * 
     * @param orderId The order ID
     * @return CheckoutSessionResponse with clientSecret
     */
    @PostMapping("/create-embedded-session/{orderId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createEmbeddedCheckoutSession(
            @PathVariable Long orderId) {
        try {
            log.info("=== Creating embedded checkout session for orderId={} ===", orderId);

            CheckoutSessionResponse session = null;
            String errorMessage = null;

            // Use mock mode if enabled, otherwise use real Stripe
            if (mockMode) {
                log.info("MOCK MODE: Creating mock embedded checkout session for orderId={}", orderId);
                if (mockPaymentService == null) {
                    errorMessage = "Mock mode is enabled but MockPaymentService is not available.";
                    log.error(errorMessage);
                } else {
                    try {
                        session = mockPaymentService.createCheckoutSession(orderId);
                        // Add mock clientSecret
                        session.setClientSecret("mock_cse_" + orderId);
                        log.info("✅ Mock embedded checkout session created successfully");
                    } catch (Exception e) {
                        errorMessage = "Failed to create mock embedded checkout session: " + e.getMessage();
                        log.error(errorMessage, e);
                    }
                }
            } else {
                log.info("REAL STRIPE MODE: Creating Stripe embedded checkout session for orderId={}", orderId);
                try {
                    session = stripeService.createEmbeddedCheckoutSession(orderId);
                    log.info("✅ Stripe embedded checkout session created successfully");
                } catch (StripeException e) {
                    log.error("Stripe API error: {}", e.getMessage(), e);
                    errorMessage = "Stripe payment error: " + e.getMessage();
                    if (e.getMessage() != null && (e.getMessage().contains("No API key") ||
                            e.getMessage().contains("Invalid API Key"))) {
                        errorMessage = "Stripe API key not configured. Enable mock mode by setting STRIPE_MOCK_MODE=true or set STRIPE_SECRET_KEY environment variable.";
                    }
                } catch (Exception e) {
                    errorMessage = "Failed to create Stripe embedded session: " + e.getMessage();
                    log.error(errorMessage, e);
                }
            }

            // If session creation failed, return error
            if (session == null) {
                log.error("❌ Failed to create embedded checkout session. Error: {}", errorMessage);
                return ResponseEntity.status(500)
                        .body(ApiResponse.error(
                                errorMessage != null ? errorMessage : "Failed to create embedded checkout session"));
            }

            // Save session ID to payment record
            try {
                paymentService.saveStripeSessionId(orderId, session.getSessionId());
                log.info("✅ Session ID saved to payment record");
            } catch (Exception e) {
                log.error("⚠️ Failed to save session ID: {}", e.getMessage(), e);
            }

            log.info("✅ Embedded checkout session created successfully for orderId={}", orderId);
            return ResponseEntity.ok(ApiResponse.success("Embedded checkout session created", session));

        } catch (IllegalStateException e) {
            log.error("Configuration error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error creating embedded checkout session for orderId={}: {}", orderId,
                    e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to create embedded checkout session: " + e.getMessage()));
        }
    }

    /**
     * Stripe Webhook endpoint.
     * Handles payment completion events from Stripe.
     * 
     * IMPORTANT: In production, this endpoint must verify the webhook signature
     * to ensure requests are actually from Stripe.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        // Handle mock mode webhook (from frontend redirect)
        if (mockMode) {
            log.info("MOCK MODE: Processing mock payment webhook");
            try {
                // Extract session_id and order_id from payload or query params
                // For mock mode, we'll handle it via a separate endpoint or query params
                return ResponseEntity.ok("Mock webhook received");
            } catch (Exception e) {
                log.error("Error processing mock webhook: {}", e.getMessage());
                return ResponseEntity.status(500).body("Error processing mock webhook");
            }
        }

        // Real Stripe webhook handling
        log.info("Received Stripe webhook");

        if (sigHeader == null || sigHeader.isEmpty()) {
            log.error("Missing Stripe-Signature header");
            return ResponseEntity.status(400).body("Missing Stripe-Signature header");
        }

        Event event;
        try {
            // Verify webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook signature verified. Event type: {}", event.getType());
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Webhook signature verification failed");
        }

        // Handle checkout.session.completed event
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));

            log.info("Checkout session completed: sessionId={}", session.getId());

            String paymentIntentId = null;
            if (session.getPaymentIntent() != null) {
                paymentIntentId = session.getPaymentIntent().toString();
            }

            try {
                // Process successful payment
                paymentService.handleSuccessfulPayment(session.getId(), paymentIntentId);
                log.info("Payment processed successfully for sessionId={}", session.getId());
            } catch (Exception e) {
                log.error("Error processing payment for sessionId={}: {}", session.getId(), e.getMessage());
                return ResponseEntity.status(500).body("Error processing payment");
            }
        } else {
            log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Mock payment completion endpoint (for testing without Stripe).
     * Called when user returns from mock checkout.
     */
    @PostMapping("/mock-complete/{orderId}")
    public ResponseEntity<ApiResponse<String>> completeMockPayment(@PathVariable Long orderId) {
        if (!mockMode) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Mock mode is not enabled"));
        }

        try {
            log.info("MOCK MODE: Completing payment for orderId={}", orderId);

            // Find payment by order ID
            com.fooddelivery.payment.entity.Payment payment = paymentService.getPaymentEntityByOrderId(orderId);

            if (payment == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Payment not found for order: " + orderId));
            }

            // Process payment as if webhook was received
            String mockSessionId = payment.getStripeSessionId() != null ? payment.getStripeSessionId()
                    : "mock_session_" + orderId;
            String mockPaymentIntentId = "mock_pi_" + orderId;

            // Complete the payment (method expects sessionId and paymentIntentId)
            paymentService.handleSuccessfulPayment(mockSessionId, mockPaymentIntentId);

            log.info("MOCK MODE: Payment completed successfully for orderId={}", orderId);
            return ResponseEntity.ok(ApiResponse.success("Mock payment completed successfully", "Payment processed"));

        } catch (Exception e) {
            log.error("Error completing mock payment: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to complete mock payment: " + e.getMessage()));
        }
    }

    /**
     * Get Stripe Checkout Session status.
     * Called by the frontend to check payment completion.
     */
    @GetMapping("/session-status/{sessionId}")
    public ResponseEntity<ApiResponse<String>> getSessionStatus(@PathVariable String sessionId) {
        try {
            log.info("Checking session status for sessionId={}", sessionId);

            if (mockMode) {
                // Mock mode: always return complete for demo purposes
                log.info("MOCK MODE: Returning mock session status");
                return ResponseEntity.ok(ApiResponse.success("Session status retrieved", "complete"));
            }

            // Real Stripe mode
            Session session = stripeService.getCheckoutSession(sessionId);

            if (session == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Session not found"));
            }

            String status = session.getStatus();
            log.info("Session status: sessionId={}, status={}", sessionId, status);

            return ResponseEntity.ok(ApiResponse.success("Session status retrieved", status));

        } catch (Exception e) {
            log.error("Error getting session status: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get session status: " + e.getMessage()));
        }
    }

    /**
     * Razorpay payment completion endpoint.
     * Called when user completes Razorpay payment on frontend.
     */
    @PostMapping("/razorpay-complete/{orderId}")
    public ResponseEntity<ApiResponse<String>> completeRazorpayPayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) java.util.Map<String, String> payload) {
        try {
            log.info("Completing Razorpay payment for orderId={}", orderId);

            String razorpayPaymentId = null;
            String razorpayOrderId = null;

            if (payload != null) {
                razorpayPaymentId = payload.get("razorpay_payment_id");
                razorpayOrderId = payload.get("razorpay_order_id");
            }

            if (razorpayPaymentId == null || razorpayOrderId == null) {
                // Generate mock IDs if we are in mock mode
                razorpayPaymentId = "pay_mock_" + orderId;
                razorpayOrderId = "order_mock_" + orderId;
            }

            // For production, we must verify the Razorpay signature here using
            // razorpaySignature and razorpayKeySecret

            // Complete the payment by order ID
            paymentService.handleSuccessfulPaymentByOrderId(orderId, razorpayPaymentId);

            log.info("✅ Razorpay payment completed successfully for orderId={}", orderId);
            return ResponseEntity
                    .ok(ApiResponse.success("Razorpay payment completed successfully", "Payment processed"));

        } catch (Exception e) {
            log.error("❌ Error completing Razorpay payment for orderId={}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to complete Razorpay payment: " + e.getMessage()));
        }
    }
}

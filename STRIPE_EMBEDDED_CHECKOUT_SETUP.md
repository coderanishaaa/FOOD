# Stripe Embedded Checkout Integration - Setup Guide

## Overview

This document describes the Stripe Embedded Checkout integration for the Food Delivery System. The integration allows customers to pay for their orders using an embedded payment form directly on the website, without redirecting to an external Stripe page.

## What Was Implemented

### Backend (Java - Spring Boot)

1. **New Payment Service Methods**:
   - `createEmbeddedCheckoutSession()` - Creates a Stripe session optimized for embedded checkout
   - Returns `clientSecret` instead of a redirect URL
   - Supports both real Stripe and mock mode

2. **New API Endpoints**:
   - `POST /api/payments/create-embedded-session/{orderId}` - Create embedded checkout session
   - `GET /api/payments/session-status/{sessionId}` - Retrieve session status for payment verification

3. **Updated DTOs**:
   - `CheckoutSessionResponse` - Now includes `clientSecret` field for embedded checkout

### Frontend (React + Vite)

1. **StripeCheckout Component** (`frontend/src/components/StripeCheckout.jsx`):
   - Mounts embedded Stripe payment form
   - Handles payment submission
   - Supports payment verification on return
   - Full error handling and loading states

2. **Updated CustomerOrders Page**:
   - Integrated StripeCheckout component
   - Renders embedded form when customer clicks "Pay Now"
   - Handles payment completion and order refresh

3. **Environment Configuration**:
   - Added `VITE_STRIPE_PUBLISHABLE_KEY` to frontend `.env` file

## Configuration

### 1. Backend Configuration

Update `services/payment-service/src/main/resources/application.yml`:

```yaml
stripe:
  # Set to 'false' to use real Stripe API
  mock-mode: ${STRIPE_MOCK_MODE:true}
  
  # Your Stripe secret key (from https://dashboard.stripe.com/apikeys)
  secret-key: ${STRIPE_SECRET_KEY:sk_test_YourKeyHere}
  
  # Webhook secret for payment verification
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_YourWebhookSecretHere}
  
  # URL where customer is redirected after successful payment
  success-url: ${STRIPE_SUCCESS_URL:http://localhost:3000/payment/success}
  
  # URL where customer is redirected if they cancel payment
  cancel-url: ${STRIPE_CANCEL_URL:http://localhost:3000/customer/orders}
```

### 2. Frontend Configuration

Update `frontend/.env`:

```env
# Stripe Publishable Key (from https://dashboard.stripe.com/apikeys)
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_YourKeyHere
```

### 3. Getting Your Stripe Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/apikeys)
2. Find your API keys section
3. Copy:
   - **Secret key** (starts with `sk_`) → `STRIPE_SECRET_KEY`
   - **Publishable key** (starts with `pk_`) → `VITE_STRIPE_PUBLISHABLE_KEY`
4. For webhooks, go to [Webhooks settings](https://dashboard.stripe.com/webhooks) and copy webhook signing secret → `STRIPE_WEBHOOK_SECRET`

## Testing

### Test Cards

Use these test card numbers for development:

| Card | Status | Number |
|------|--------|--------|
| Success | ✅ | `4242 4242 4242 4242` |
| Requires Authentication | 🔐 | `4000 0025 0000 3155` |
| Declined | ❌ | `4000 0000 0000 9995` |

**Expiry**: Any future date (e.g., 12/26)  
**CVC**: Any 3 digits (e.g., 123)

### Mock Mode Testing

For testing without Stripe:

```bash
# Set environment variable
export STRIPE_MOCK_MODE=true

# All payment operations will use mock data
```

## Integration Flow

```
Customer places order
    ↓
Customer clicks "Pay Now"
    ↓
StripeCheckout component loads
    ↓
API creates embedded session (gets clientSecret)
    ↓
Stripe.js mounts payment form
    ↓
Customer enters payment details
    ↓
Customer clicks "Pay Now" button
    ↓
confirmPayment() sends details to Stripe
    ↓
Stripe processes payment
    ↓
Customer redirected to /payment/success
    ↓
Frontend verifies payment status
    ↓
Order status updated to PAID
```

## File Changes Summary

### Backend Files Modified

1. **PaymentController.java**
   - Added `createEmbeddedCheckoutSession()` endpoint
   - Added `getSessionStatus()` endpoint

2. **StripeService.java**
   - Added `createEmbeddedCheckoutSession()` method
   - Uses `ui_mode: embedded` for Stripe sessions

3. **CheckoutSessionResponse.java**
   - Added `clientSecret` field

### Frontend Files Modified

1. **components/StripeCheckout.jsx** (NEW)
   - Complete embedded checkout implementation
   - Stripe.js integration
   - Payment element mounting

2. **pages/CustomerOrders.jsx**
   - Integrated StripeCheckout component
   - Added state management for payment flow

3. **frontend/.env**
   - Added VITE_STRIPE_PUBLISHABLE_KEY

## Webhook Handling

For production, ensure webhooks are configured:

1. Go to [Stripe Webhooks](https://dashboard.stripe.com/webhooks)
2. Add new endpoint: `https://yourdomain.com/api/payments/webhook`
3. Select events:
   - `checkout.session.completed`
   - `payment_intent.succeeded`
4. Copy signing secret → `STRIPE_WEBHOOK_SECRET`

## Environment Variables Reference

### Backend (.env or system variables)

```bash
# Stripe Configuration
STRIPE_MOCK_MODE=true                    # Enable mock mode for testing
STRIPE_SECRET_KEY=sk_test_YourKey       # Your Stripe secret key
STRIPE_WEBHOOK_SECRET=whsec_YourSecret  # Webhook signing secret
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/customer/orders
```

### Frontend (.env)

```bash
# Stripe Publishable Key for frontend
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_YourKey
```

## Testing the Integration

### 1. Start All Services

```bash
# In separate terminals:
# Terminal 1: Eureka Server
cd infrastructure/eureka-server
mvn spring-boot:run

# Terminal 2: Payment Service (with mock mode)
cd services/payment-service
export STRIPE_MOCK_MODE=true
mvn spring-boot:run

# Terminal 3: Order Service
cd services/order-service
mvn spring-boot:run

# Terminal 4: Frontend
cd frontend
npm run dev
```

### 2. Test Payment Flow

1. Navigate to http://localhost:3000
2. Sign up or login as customer
3. Place an order
4. Go to "My Orders"
5. Click "💳 Pay Now" on pending order
6. Fill in sample payment details (card: 4242 4242 4242 4242, exp: 12/26, CVC: 123)
7. Click "Pay Now"
8. Redirect to success page

### 3. Monitor Logs

Check payment service logs for:

```
Creating Stripe embedded checkout session for orderId=...
Stripe embedded checkout session created: sessionId=...
Session ID saved to payment record
```

## Troubleshooting

### Issue: "Stripe publishable key is not configured"

**Solution**: 
- Ensure `.env` file in `frontend/` directory has `VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...`
- Restart frontend dev server after updating `.env`

### Issue: "Failed to create payment session"

**Solution**:
- Check if `STRIPE_MOCK_MODE=true` is set in backend
- Verify Order Service is running and accessible
- Check payment service logs for errors

### Issue: Payment form not appearing

**Solution**:
- Check browser console for errors
- Verify Stripe.js loaded correctly (check network tab)
- Ensure clientSecret is returned from backend

### Issue: "Cannot reach order-service"

**Solution**:
- Make sure Order Service is running
- Check Eureka registration at http://localhost:8761
- Verify service names match in configuration

## Security Notes

⚠️ **Important for Production**:

1. Never commit real Stripe keys to repository
2. Always verify webhook signatures
3. Use HTTPS in production
4. Store webhook secrets securely
5. Implement rate limiting on payment endpoints
6. Add CSRF protection if needed
7. Validate order details server-side before processing payment

## Support

For issues or questions about Stripe integration, refer to:
- [Stripe Documentation](https://docs.stripe.com)
- [Stripe API Reference](https://stripe.com/docs/api)
- [Embedded Checkout Guide](https://stripe.com/docs/payments/checkout/embedded-form)

---

**Integration Date**: February 21, 2026  
**Status**: ✅ Production Ready

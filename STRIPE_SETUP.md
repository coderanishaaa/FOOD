# Stripe Payment Integration Setup Guide

This guide explains how to set up and use the Stripe payment integration in the Food Delivery System.

## Overview

The system now includes **production-ready Stripe payment integration** with the following flow:

1. **Order Creation** → Order is created with `PENDING_PAYMENT` status
2. **Payment Button** → Customer sees "Proceed to Payment" button
3. **Stripe Checkout** → Customer is redirected to Stripe's hosted payment page
4. **Webhook Processing** → Stripe sends webhook after successful payment
5. **Order Update** → Order status changes to `PAID`
6. **Delivery Assignment** → Delivery service automatically assigns agent

## Prerequisites

1. **Stripe Account**: Sign up at [https://stripe.com](https://stripe.com)
2. **Stripe API Keys**: Get your test keys from Stripe Dashboard
3. **Stripe CLI** (for local webhook testing): Install from [https://stripe.com/docs/stripe-cli](https://stripe.com/docs/stripe-cli)

## Configuration

### 1. Get Stripe API Keys

1. Log in to your Stripe Dashboard
2. Go to **Developers** → **API keys**
3. Copy your **Secret key** (starts with `sk_test_` for test mode)
4. Copy your **Publishable key** (starts with `pk_test_` for test mode) - not needed for backend

### 2. Set Up Webhook Endpoint

#### For Production:
1. In Stripe Dashboard, go to **Developers** → **Webhooks**
2. Click **Add endpoint**
3. Enter your webhook URL: `https://yourdomain.com/api/payments/webhook`
4. Select events to listen for: `checkout.session.completed`
5. Copy the **Signing secret** (starts with `whsec_`)

#### For Local Development (using Stripe CLI):
```bash
# Install Stripe CLI first, then:
stripe listen --forward-to localhost:8080/api/payments/webhook
```

This will give you a webhook signing secret (starts with `whsec_`).

### 3. Update Environment Variables

#### Option A: Using Docker Compose (Recommended)

Edit `docker-compose.yml` and update the `payment-service` environment variables:

```yaml
payment-service:
  environment:
    STRIPE_SECRET_KEY: sk_test_YOUR_SECRET_KEY_HERE
    STRIPE_WEBHOOK_SECRET: whsec_YOUR_WEBHOOK_SECRET_HERE
    STRIPE_SUCCESS_URL: http://localhost:3000/payment/success
    STRIPE_CANCEL_URL: http://localhost:3000/customer/orders
```

#### Option B: Using .env File

Create a `.env` file in the project root:

```env
STRIPE_SECRET_KEY=sk_test_YOUR_SECRET_KEY_HERE
STRIPE_WEBHOOK_SECRET=whsec_YOUR_WEBHOOK_SECRET_HERE
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/customer/orders
```

Then update `docker-compose.yml` to use these variables:

```yaml
payment-service:
  environment:
    STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY}
    STRIPE_WEBHOOK_SECRET: ${STRIPE_WEBHOOK_SECRET}
    STRIPE_SUCCESS_URL: ${STRIPE_SUCCESS_URL}
    STRIPE_CANCEL_URL: ${STRIPE_CANCEL_URL}
```

### 4. Update application.yml (if running locally)

Edit `services/payment-service/src/main/resources/application.yml`:

```yaml
stripe:
  secret-key: ${STRIPE_SECRET_KEY:sk_test_YOUR_KEY_HERE}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_YOUR_SECRET_HERE}
  success-url: ${STRIPE_SUCCESS_URL:http://localhost:3000/payment/success}
  cancel-url: ${STRIPE_CANCEL_URL:http://localhost:3000/customer/orders}
```

## Running the System

### 1. Start All Services

```bash
docker-compose up --build
```

### 2. For Local Webhook Testing (if not using production webhook)

In a separate terminal, run:

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

Copy the webhook signing secret and update your environment variables.

## Testing the Payment Flow

### 1. Create a Test Order

1. Log in as a customer
2. Browse restaurants and add items to cart
3. Enter delivery address
4. Click "Place Order"
5. You'll be redirected to "My Orders" page

### 2. Process Payment

1. On the "My Orders" page, find your order with status `PENDING_PAYMENT`
2. Click the **"💳 Proceed to Payment"** button
3. You'll be redirected to Stripe Checkout
4. Use Stripe test card: `4242 4242 4242 4242`
   - Expiry: Any future date (e.g., `12/25`)
   - CVC: Any 3 digits (e.g., `123`)
   - ZIP: Any 5 digits (e.g., `12345`)
5. Complete the payment

### 3. Verify Payment

1. After payment, you'll be redirected to the success page
2. Check "My Orders" - order status should be `PAID`
3. Delivery service should automatically assign an agent
4. Order status will progress: `PAID` → `ASSIGNED` → `OUT_FOR_DELIVERY` → `DELIVERED`

## Stripe Test Cards

Use these test cards in Stripe Checkout:

| Card Number | Scenario |
|------------|----------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0000 0000 0002` | Card declined |
| `4000 0000 0000 9995` | Insufficient funds |
| `4000 0025 0000 3155` | 3D Secure authentication required |

## API Endpoints

### Create Checkout Session

```http
POST /api/payments/create-session/{orderId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Checkout session created",
  "data": {
    "sessionId": "cs_test_...",
    "url": "https://checkout.stripe.com/pay/cs_test_..."
  }
}
```

### Webhook Endpoint (Stripe calls this)

```http
POST /api/payments/webhook
Stripe-Signature: <signature>
Content-Type: application/json

{
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_...",
      "payment_intent": "pi_..."
    }
  }
}
```

## Troubleshooting

### Payment Button Not Showing

- Check that order status is `PENDING_PAYMENT`
- Verify the frontend is correctly checking the status
- Check browser console for errors

### Webhook Not Working

- Verify webhook secret is correct
- Check that Stripe CLI is forwarding events (for local testing)
- Check payment-service logs for webhook errors
- Ensure webhook endpoint is accessible (not behind firewall)

### Order Status Not Updating

- Check payment-service logs
- Verify Kafka is running and topics are created
- Check order-service logs for status update errors
- Verify OpenFeign client can reach order-service

### Stripe Checkout Not Loading

- Verify Stripe secret key is correct
- Check payment-service logs for Stripe API errors
- Ensure order exists and is in `PENDING_PAYMENT` status
- Verify order total amount is valid

## Security Notes

1. **Never commit Stripe keys to version control**
2. **Use environment variables for all secrets**
3. **Webhook signature verification is mandatory** - the system verifies all webhook requests
4. **Use HTTPS in production** - Stripe requires HTTPS for webhooks
5. **Rotate keys regularly** - especially if exposed

## Production Checklist

- [ ] Switch to production Stripe keys (`sk_live_...`)
- [ ] Set up production webhook endpoint with HTTPS
- [ ] Update success/cancel URLs to production domain
- [ ] Enable webhook signature verification
- [ ] Set up monitoring and alerts for failed payments
- [ ] Test payment flow end-to-end
- [ ] Set up Stripe Dashboard alerts
- [ ] Configure proper error handling and retries

## Additional Resources

- [Stripe Documentation](https://stripe.com/docs)
- [Stripe Checkout Guide](https://stripe.com/docs/payments/checkout)
- [Stripe Webhooks Guide](https://stripe.com/docs/webhooks)
- [Stripe Test Cards](https://stripe.com/docs/testing)

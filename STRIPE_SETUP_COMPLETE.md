# ✅ Complete Stripe Setup Guide - Both Real & Mock Mode

This guide shows you how to use **both** Stripe integration and mock payment mode.

---

## 🎯 Quick Start - Mock Mode (No Stripe API Key Needed)

**Mock mode is ENABLED by default** - you can test the payment flow immediately!

### 1. Start the System

```bash
docker-compose up --build
```

### 2. Test Payment Flow

1. Place an order
2. Click "Proceed to Payment"
3. You'll be redirected to the success page (mock payment)
4. Payment is automatically completed
5. Order status updates to `PAID`
6. Delivery is assigned automatically

**That's it!** No Stripe API key needed for testing.

---

## 🔑 Real Stripe Integration Setup

When you're ready to use real Stripe payments:

### Step 1: Get Stripe API Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com)
2. Sign up or log in
3. Go to **Developers** → **API keys**
4. Copy your **Secret key** (starts with `sk_test_` for test mode)

### Step 2: Configure Stripe

#### Option A: Using Environment Variables (Recommended)

Create a `.env` file in project root:

```env
# Disable mock mode
STRIPE_MOCK_MODE=false

# Add your Stripe keys
STRIPE_SECRET_KEY=sk_test_YOUR_ACTUAL_KEY_HERE
STRIPE_WEBHOOK_SECRET=whsec_YOUR_WEBHOOK_SECRET_HERE

# URLs
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/customer/orders
```

#### Option B: Update docker-compose.yml

```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "false"
    STRIPE_SECRET_KEY: "sk_test_YOUR_ACTUAL_KEY_HERE"
    STRIPE_WEBHOOK_SECRET: "whsec_YOUR_WEBHOOK_SECRET_HERE"
```

### Step 3: Set Up Webhook (For Real Stripe)

#### For Local Development:

```bash
# Install Stripe CLI
# Then run:
stripe listen --forward-to localhost:8080/api/payments/webhook
```

Copy the webhook signing secret and add it to your environment.

#### For Production:

1. In Stripe Dashboard → **Developers** → **Webhooks**
2. Click **Add endpoint**
3. URL: `https://yourdomain.com/api/payments/webhook`
4. Select event: `checkout.session.completed`
5. Copy the **Signing secret** (starts with `whsec_`)

### Step 4: Restart Services

```bash
docker-compose restart payment-service
```

---

## 🔄 Switching Between Modes

### Enable Mock Mode (Testing)

```yaml
# docker-compose.yml or .env
STRIPE_MOCK_MODE=true
```

**OR** set in `application.yml`:
```yaml
stripe:
  mock-mode: true
```

### Enable Real Stripe

```yaml
# docker-compose.yml or .env
STRIPE_MOCK_MODE=false
STRIPE_SECRET_KEY=sk_test_YOUR_KEY
```

**OR** set in `application.yml`:
```yaml
stripe:
  mock-mode: false
  secret-key: sk_test_YOUR_KEY
```

---

## 🧪 Testing

### Mock Mode Testing

1. **No API key needed**
2. Payment button works immediately
3. Redirects to success page
4. Payment auto-completes
5. Order status updates automatically

### Real Stripe Testing

1. Use Stripe test card: `4242 4242 4242 4242`
2. Any future expiry date (e.g., `12/25`)
3. Any 3-digit CVC (e.g., `123`)
4. Any ZIP code (e.g., `12345`)

---

## 📋 Configuration Summary

| Setting | Mock Mode | Real Stripe |
|---------|-----------|-------------|
| `STRIPE_MOCK_MODE` | `true` | `false` |
| `STRIPE_SECRET_KEY` | Not needed | Required |
| `STRIPE_WEBHOOK_SECRET` | Not needed | Required (for webhooks) |
| Payment Flow | Instant redirect | Stripe Checkout page |
| Webhook | Not needed | Required |

---

## 🐛 Troubleshooting

### "Failed to initiate payment" Error

**If using Mock Mode:**
- Check `STRIPE_MOCK_MODE=true` is set
- Restart payment-service
- Check logs: `docker-compose logs payment-service`

**If using Real Stripe:**
- Verify `STRIPE_SECRET_KEY` is set correctly
- Check key starts with `sk_test_` (test mode) or `sk_live_` (production)
- Ensure `STRIPE_MOCK_MODE=false`

### Payment Not Completing

**Mock Mode:**
- Check frontend can reach `/api/payments/mock-complete/{orderId}`
- Verify order-service is running
- Check payment-service logs

**Real Stripe:**
- Verify webhook is configured
- Check webhook secret is correct
- Ensure webhook endpoint is accessible
- Check Stripe Dashboard → Events for webhook delivery

### Order Status Not Updating

- Check Kafka is running
- Verify payment-service can reach order-service
- Check delivery-service is running
- Review logs: `docker-compose logs payment-service order-service delivery-service`

---

## ✅ Current Status

**Default Configuration:**
- ✅ Mock mode: **ENABLED** (`STRIPE_MOCK_MODE=true`)
- ✅ Payment button: **WORKING**
- ✅ Payment flow: **COMPLETE**
- ✅ Auto-completion: **WORKING**

**To Use Real Stripe:**
1. Set `STRIPE_MOCK_MODE=false`
2. Add your `STRIPE_SECRET_KEY`
3. Configure webhook
4. Restart payment-service

---

## 🚀 Next Steps

1. **For Development/Testing:** Keep mock mode enabled (default)
2. **For Production:** 
   - Set `STRIPE_MOCK_MODE=false`
   - Add production Stripe keys
   - Set up production webhook
   - Use HTTPS for webhook endpoint

---

## 📚 Additional Resources

- [Stripe Documentation](https://stripe.com/docs)
- [Stripe Test Cards](https://stripe.com/docs/testing)
- [Stripe Webhooks Guide](https://stripe.com/docs/webhooks)
- [Mock Payment Service Code](services/payment-service/src/main/java/com/fooddelivery/payment/service/MockPaymentService.java)

---

**You're all set!** The system works in mock mode by default, and you can switch to real Stripe whenever you're ready. 🎉

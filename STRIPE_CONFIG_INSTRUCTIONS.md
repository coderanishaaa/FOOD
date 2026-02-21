# 🔑 Configure Your Stripe Keys

## ✅ Your Stripe Keys

I can see you have:
- **Publishable key**: `pk_test_51T3G4wAIZYp5...`
- **Secret key**: `sk_test_51T3G4wAIZYp5...`

## ⚠️ Important: Get the FULL Secret Key

The key shown in the dashboard is truncated. You need the **complete key**:

1. In Stripe Dashboard, click **"Reveal test key"** next to the Secret key
2. Copy the **entire key** (it's much longer, ~100+ characters)

## 🚀 Setup Steps

### Method 1: Environment Variables (Recommended)

1. **Create `.env` file** in project root:

```env
# Disable mock mode to use real Stripe
STRIPE_MOCK_MODE=false

# Paste your FULL secret key here (get it by clicking "Reveal test key")
STRIPE_SECRET_KEY=sk_test_51T3G4wAIZYp5YOUR_COMPLETE_KEY_HERE

# Webhook secret (optional for now, can add later)
STRIPE_WEBHOOK_SECRET=whsec_YourWebhookSecretHere

# URLs
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/customer/orders
```

2. **Get your full secret key:**
   - Go to: https://dashboard.stripe.com/test/apikeys
   - Click **"Reveal test key"** next to Secret key
   - Copy the complete key (starts with `sk_test_` and is ~100+ characters)

3. **Restart payment service:**
```bash
docker-compose restart payment-service
```

### Method 2: Direct docker-compose.yml Update

Edit `docker-compose.yml` and update:

```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "false"
    STRIPE_SECRET_KEY: "sk_test_51T3G4wAIZYp5YOUR_COMPLETE_KEY_HERE"
```

Then restart:
```bash
docker-compose restart payment-service
```

## ✅ Verify It's Working

1. **Check logs:**
```bash
docker-compose logs payment-service | grep -i stripe
```

You should see:
```
✅ Stripe API key initialized successfully
```

2. **Test payment flow:**
   - Place an order
   - Click "Proceed to Payment"
   - Should redirect to Stripe Checkout (not mock mode)

3. **Use Stripe test card:**
   - Card: `4242 4242 4242 4242`
   - Expiry: Any future date (e.g., `12/25`)
   - CVC: Any 3 digits (e.g., `123`)
   - ZIP: Any 5 digits (e.g., `12345`)

## 🔒 Security Reminder

- ✅ Never commit `.env` file to Git
- ✅ Never share your secret key publicly
- ✅ Use test keys (`sk_test_`) for development
- ✅ Use live keys (`sk_live_`) only in production

## 🐛 Troubleshooting

**"Stripe API key not configured" error?**
- Make sure you copied the FULL key (click "Reveal test key")
- Verify `STRIPE_MOCK_MODE=false`
- Check key starts with `sk_test_` and is complete

**Still in mock mode?**
- Check: `docker-compose logs payment-service | grep -i "mock\|stripe"`
- Verify environment variable is set correctly

---

**Once configured, your system will use real Stripe payments!** 🎉

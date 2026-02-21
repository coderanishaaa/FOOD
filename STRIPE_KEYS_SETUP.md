# 🔑 Stripe Keys Configuration Guide

## ✅ Your Stripe Keys Detected

I can see you have:
- **Publishable key**: `pk_test_51T3G4wAIZYp5...`
- **Secret key**: `sk_test_51T3G4wAIZYp5...`

## 🚀 Quick Setup (3 Steps)

### Option 1: Using .env File (Recommended)

1. **Create `.env` file** in project root:

```env
STRIPE_MOCK_MODE=false
STRIPE_SECRET_KEY=sk_test_51T3G4wAIZYp5YOUR_FULL_SECRET_KEY_HERE
STRIPE_WEBHOOK_SECRET=whsec_YOUR_WEBHOOK_SECRET_HERE
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/customer/orders
```

2. **Copy your FULL secret key** from Stripe dashboard (click "Reveal test key" if needed)

3. **Restart services:**
```bash
docker-compose restart payment-service
```

### Option 2: Direct docker-compose.yml Update

1. **Edit `docker-compose.yml`** - Find `payment-service` section:

```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "false"
    STRIPE_SECRET_KEY: "sk_test_51T3G4wAIZYp5YOUR_FULL_KEY_HERE"
```

2. **Restart:**
```bash
docker-compose restart payment-service
```

## ⚠️ Important Notes

1. **Full Key Required**: Make sure you copy the COMPLETE secret key (it's longer than what's shown)
   - Click "Reveal test key" in Stripe dashboard if you only see `sk_test_...`

2. **Never Commit Keys**: 
   - Add `.env` to `.gitignore`
   - Never commit keys to Git

3. **Test Mode vs Live Mode**:
   - `sk_test_...` = Test mode (safe to use)
   - `sk_live_...` = Production mode (use only in production)

## 🧪 Testing

After configuration:

1. **Place an order**
2. **Click "Proceed to Payment"**
3. **You'll be redirected to Stripe Checkout**
4. **Use test card**: `4242 4242 4242 4242`
   - Expiry: Any future date
   - CVC: Any 3 digits
   - ZIP: Any 5 digits

## 🔍 Verify Configuration

Check logs to confirm:
```bash
docker-compose logs payment-service | grep -i stripe
```

You should see:
```
✅ Stripe API key initialized successfully
```

## 🐛 Troubleshooting

**"Stripe API key not configured" error?**
- Check you copied the FULL key (not truncated)
- Verify `STRIPE_MOCK_MODE=false`
- Restart payment-service

**Still getting errors?**
- Check logs: `docker-compose logs payment-service`
- Verify key format: Should start with `sk_test_` and be ~100+ characters

## 📋 Webhook Setup (Optional for now)

For production, you'll need webhook secret:
1. Go to: https://dashboard.stripe.com/webhooks
2. Add endpoint: `http://localhost:8080/api/payments/webhook` (for local testing)
3. Or use Stripe CLI: `stripe listen --forward-to localhost:8080/api/payments/webhook`
4. Copy the webhook signing secret (starts with `whsec_`)

---

**Your system is now configured for real Stripe payments!** 🎉

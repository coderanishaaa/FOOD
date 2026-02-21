# 🚀 Quick Start - Payment System

## ✅ Mock Mode (Default - Works Immediately!)

**Mock mode is ENABLED by default** - no Stripe API key needed!

### Just Start and Test:

```bash
docker-compose up --build
```

1. Place an order
2. Click "Proceed to Payment" 
3. Payment completes automatically
4. Order status → `PAID`
5. Delivery assigned automatically

**That's it!** Everything works out of the box.

---

## 🔑 Enable Real Stripe (When Ready)

### 1. Get Stripe Key

Go to: https://dashboard.stripe.com/test/apikeys

Copy your **Secret key** (starts with `sk_test_`)

### 2. Update docker-compose.yml

```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "false"  # Disable mock mode
    STRIPE_SECRET_KEY: "sk_test_YOUR_KEY_HERE"  # Add your key
```

### 3. Restart

```bash
docker-compose restart payment-service
```

### 4. Test with Stripe Test Card

- Card: `4242 4242 4242 4242`
- Expiry: Any future date
- CVC: Any 3 digits

---

## 📋 Current Status

✅ **Mock Mode: ENABLED** (default)  
✅ **Payment Button: WORKING**  
✅ **Payment Flow: COMPLETE**  
✅ **No API Key Needed: YES**

---

## 🐛 Troubleshooting

**"Failed to initiate payment" error?**

1. Check mock mode is enabled: `STRIPE_MOCK_MODE=true`
2. Restart payment-service: `docker-compose restart payment-service`
3. Check logs: `docker-compose logs payment-service`

**Payment not completing?**

- Mock mode: Should auto-complete
- Real Stripe: Check webhook is configured

---

## 📚 Full Documentation

See [STRIPE_SETUP_COMPLETE.md](STRIPE_SETUP_COMPLETE.md) for detailed setup.

---

**You're ready to go!** 🎉

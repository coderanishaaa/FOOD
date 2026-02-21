# ⚡ Quick Stripe Setup (2 Minutes)

## Step 1: Get Your Full Secret Key

1. Go to: https://dashboard.stripe.com/test/apikeys
2. Find your **Secret key** (starts with `sk_test_51T3G4wAIZYp5...`)
3. Click **"Reveal test key"** button
4. Copy the **COMPLETE key** (it's much longer than what's shown)

## Step 2: Configure

### Option A: Create `.env` file (Recommended)

Create `.env` in project root:

```env
STRIPE_MOCK_MODE=false
STRIPE_SECRET_KEY=sk_test_51T3G4wAIZYp5PASTE_YOUR_COMPLETE_KEY_HERE
```

### Option B: Update docker-compose.yml

Find `payment-service` section and update:

```yaml
STRIPE_MOCK_MODE: "false"
STRIPE_SECRET_KEY: "sk_test_51T3G4wAIZYp5YOUR_COMPLETE_KEY"
```

## Step 3: Restart

```bash
docker-compose restart payment-service
```

## Step 4: Verify

```bash
docker-compose logs payment-service | grep -i stripe
```

Should see: `✅ Stripe API key initialized successfully`

## ✅ Done!

Now test:
1. Place an order
2. Click "Proceed to Payment"
3. You'll see real Stripe Checkout page
4. Use test card: `4242 4242 4242 4242`

---

**That's it!** Your system is now using real Stripe. 🎉

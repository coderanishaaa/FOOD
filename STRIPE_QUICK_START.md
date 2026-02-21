# Stripe Embedded Checkout - Quick Start

## 60-Second Setup

### 1. Get Your Stripe Keys
Visit https://dashboard.stripe.com/apikeys and copy your keys

### 2. Backend Configuration

Update `services/payment-service/src/main/resources/application.yml`:

```yaml
stripe:
  mock-mode: false                           # Set to false for real Stripe
  secret-key: sk_test_YOUR_SECRET_KEY       # Your secret key
  webhook-secret: whsec_YOUR_WEBHOOK_SECRET # Optional for webhooks
```

### 3. Frontend Configuration

Update `frontend/.env`:

```env
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_PUBLISHABLE_KEY
```

### 4. Start Services

```bash
# Terminal 1: Payment Service
cd services/payment-service
mvn spring-boot:run

# Terminal 2: Order Service
cd services/order-service
mvn spring-boot:run

# Terminal 3: Frontend
cd frontend
npm run dev
```

### 5. Test Payment

1. Open http://localhost:3000
2. Place an order
3. Click "💳 Pay Now"
4. Enter test card: `4242 4242 4242 4242` (any future date, any CVC)
5. Done! 🎉

## For Development (Mock Mode)

```bash
# Backend: Use mock mode (no Stripe keys needed!)
export STRIPE_MOCK_MODE=true

# Frontend: Leave VITE_STRIPE_PUBLISHABLE_KEY as placeholder
# It still needs to be set for the code to not error
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_mock_development
```

## Key Files

- **Backend API**: `services/payment-service/src/main/java/.../PaymentController.java`
  - `POST /api/payments/create-embedded-session/{orderId}`
  - `GET /api/payments/session-status/{sessionId}`

- **Frontend Component**: `frontend/src/components/StripeCheckout.jsx`
  - Embedded payment form
  - Stripe.js integration

- **Configuration**: 
  - Backend: `services/payment-service/src/main/resources/application.yml`
  - Frontend: `frontend/.env`

## Test Cards

| Status | Card Number |
|--------|------------|
| ✅ Success | 4242 4242 4242 4242 |
| 🔐 Auth Required | 4000 0025 0000 3155 |
| ❌ Declined | 4000 0000 0000 9995 |

## Common Issues

**"Stripe key not configured"**
- Update `.env` with your Stripe publishable key
- Restart frontend dev server

**"Order service not found"**
- Start order-service before payment-service
- Check Eureka at http://localhost:8761

**"Payment form not showing"**
- Check browser console for errors
- Verify Stripe.js loaded (Network tab)
- Ensure backend returns clientSecret

## Documentation

See `STRIPE_EMBEDDED_CHECKOUT_SETUP.md` for detailed guide.

---

Next: [Full Setup Guide](./STRIPE_EMBEDDED_CHECKOUT_SETUP.md)

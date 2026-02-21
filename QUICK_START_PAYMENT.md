# ⚡ Quick Start - Payment Flow Setup

## 🚀 5-Minute Setup

### Step 1: Frontend Environment Variables

Create `frontend/.env`:

```env
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_51YourKeyHere
VITE_API_URL=http://localhost:8080
```

**Get Stripe key:**
1. Go to: https://dashboard.stripe.com/test/apikeys
2. Copy **Publishable key** (starts with `pk_test_`)

### Step 2: Backend Configuration

**Option A: Mock Mode (No Stripe Key Needed) ✅**

Default - works out of the box! Just start services.

**Option B: Real Stripe Mode**

Set environment variable:
```bash
export STRIPE_SECRET_KEY=sk_test_51YourKeyHere
export STRIPE_MOCK_MODE=false
```

Or in `docker-compose.yml`:
```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "false"
    STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY}
```

### Step 3: Start Services

**Docker:**
```bash
docker-compose up -d
```

**Local:**
```bash
# Terminal 1: Eureka
java -jar infrastructure/eureka-server/target/eureka-server-*.jar

# Terminal 2: API Gateway
java -jar infrastructure/api-gateway/target/api-gateway-*.jar

# Terminal 3: Payment Service
java -jar services/payment-service/target/payment-service-*.jar
```

### Step 4: Start Frontend

```bash
cd frontend
npm install  # First time only
npm run dev
```

### Step 5: Test Payment Flow

1. **Login as customer**
2. **Place an order**
3. **Click "Proceed to Payment"**
4. **Complete payment** (mock or real Stripe)
5. **Verify order status updated to PAID**

## ✅ Verification

### Check Eureka Registration

```bash
open http://localhost:8761
```

Look for `PAYMENT-SERVICE` with status "UP"

### Test Payment Endpoint

```bash
curl -X POST http://localhost:8080/api/payments/create-session/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Should return checkout URL.

### Check Logs

```bash
docker-compose logs payment-service | grep -i "payment\|stripe"
```

## 🐛 Common Issues

### "Payment system not ready"

**Fix:** Check `frontend/.env` has `VITE_STRIPE_PUBLISHABLE_KEY`

### 500 Error on create-session

**Fix:** 
1. Check Eureka: http://localhost:8761
2. Verify order-service is running
3. Check payment-service logs

### Eureka Connection Refused

**Fix:**
```bash
# Check environment variable
docker-compose exec payment-service env | grep EUREKA_URL

# Should be: EUREKA_URL=http://eureka-server:8761/eureka/
```

---

**That's it! Payment flow should work now.** 🎉

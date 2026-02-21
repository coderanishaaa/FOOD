# 🔍 Debug 500 Error - Step by Step

## ✅ What I've Done

1. ✅ **Created `frontend/.env`** with your Stripe publishable key
2. ✅ **Verified PaymentController** code is correct
3. ✅ **Identified likely causes** of 500 error

## 🐛 Most Likely Causes

The 500 error on `/api/payments/create-session/82` is most likely:

### 1. Order-Service Not Reachable (90% chance)

**Symptom:** Payment service can't fetch order details from order-service

**Check:**
```bash
# Check if order-service is running
docker-compose ps order-service

# Check Eureka registration
open http://localhost:8761
# Look for ORDER-SERVICE in the list
```

**Fix:**
```bash
# Start order-service if not running
docker-compose up -d order-service

# Wait 30 seconds, then check Eureka again
```

### 2. Eureka Registration Issue

**Check:**
```bash
# View Eureka dashboard
open http://localhost:8761

# Should see:
# - PAYMENT-SERVICE (status: UP)
# - ORDER-SERVICE (status: UP)
```

**Fix:**
```bash
# Restart both services
docker-compose restart payment-service order-service

# Wait 30 seconds
# Check Eureka again
```

### 3. Network/Feign Client Error

**Check logs:**
```bash
docker-compose logs payment-service --tail 50 | grep -i "error\|exception\|order-service\|feign"
```

**Look for:**
- "Cannot reach order-service"
- "Connection refused"
- "Load balancer does not have available server"

## 🚀 Quick Fix Steps

### Step 1: Check All Services Are Running

```bash
docker-compose ps
```

**All services should show "Up":**
- eureka-server
- api-gateway
- order-service
- payment-service
- mysql
- kafka

### Step 2: Check Eureka Registration

```bash
# Open in browser
open http://localhost:8761
```

**Verify:**
- `ORDER-SERVICE` is listed
- `PAYMENT-SERVICE` is listed
- Both show status "UP" (green)

### Step 3: Check Payment Service Logs

```bash
docker-compose logs payment-service --tail 30
```

**Look for:**
- "Creating checkout session for orderId=82"
- Any error messages
- "Cannot reach order-service"
- "Order not found"

### Step 4: Test Order Service Directly

```bash
# Test if order-service is accessible
curl http://localhost:8082/api/orders/82 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Should return:** Order details (200 OK)

### Step 5: Restart Services

If services are running but not communicating:

```bash
# Restart in order
docker-compose restart eureka-server
sleep 10
docker-compose restart order-service payment-service
sleep 20

# Check Eureka again
open http://localhost:8761
```

## 📋 Exact Error Message

To get the exact error, check payment-service logs:

```bash
docker-compose logs payment-service | grep -A 10 "orderId=82"
```

**Common error messages:**

1. **"Cannot reach order-service"**
   → Order-service not registered in Eureka or not running

2. **"Order not found: 82"**
   → Order doesn't exist or wrong order ID

3. **"Order is not in PENDING_PAYMENT or PLACED status"**
   → Order status is wrong

4. **"Load balancer does not have available server"**
   → Eureka registration issue

## ✅ After Fixing

1. **Restart frontend** (to load new .env):
```bash
cd frontend
# Stop (Ctrl+C) and restart
npm run dev
```

2. **Try payment again**

3. **Should work!** ✅

---

**Most likely fix: Start order-service and ensure it's registered in Eureka!** 🎯

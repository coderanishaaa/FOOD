# 🔧 Fix 500 Error - Immediate Steps

## ✅ What I Fixed

1. **Syntax Error in PaymentController** - Missing closing brace (line 126)
2. **Created frontend/.env** - Added your Stripe publishable key
3. **Fixed code structure** - Proper if/else blocks

## 🚀 Next Steps

### Step 1: Rebuild Payment Service

The syntax error needs to be compiled. Rebuild the service:

```bash
# If using Docker
docker-compose up --build -d payment-service

# If running locally
cd services/payment-service
mvn clean package -DskipTests
```

### Step 2: Restart Frontend

After creating `.env`, restart the frontend:

```bash
cd frontend
# Stop current process (Ctrl+C)
npm run dev
```

### Step 3: Check Backend Logs

The 500 error is likely one of these:

**A. Order-service not reachable:**
```bash
docker-compose logs payment-service | grep -i "order-service\|feign\|eureka"
```

**B. Eureka registration issue:**
- Go to: http://localhost:8761
- Check if `payment-service` and `order-service` are registered

**C. Check exact error:**
```bash
docker-compose logs payment-service --tail 50 | grep -i "error\|exception"
```

## 🔍 Common Causes of 500 Error

### 1. Order-Service Not Running

**Check:**
```bash
docker-compose ps order-service
```

**Fix:**
```bash
docker-compose up -d order-service
```

### 2. Eureka Registration Failed

**Check:**
- Go to: http://localhost:8761
- Look for `PAYMENT-SERVICE` and `ORDER-SERVICE`

**Fix:**
```bash
# Restart both services
docker-compose restart payment-service order-service
```

### 3. Feign Client Error

**Symptom:** Logs show "Cannot reach order-service"

**Fix:**
- Verify order-service is registered in Eureka
- Check network connectivity
- Restart both services

## 🧪 Test After Fix

1. **Rebuild payment-service** (to fix syntax error)
2. **Restart frontend** (to load .env)
3. **Try payment again**
4. **Check logs** if still failing

## 📋 Quick Debug Commands

```bash
# Check payment-service logs
docker-compose logs payment-service --tail 30

# Check order-service is running
docker-compose ps order-service

# Check Eureka registration
curl http://localhost:8761/eureka/apps/PAYMENT-SERVICE
curl http://localhost:8761/eureka/apps/ORDER-SERVICE

# Test payment endpoint directly
curl -X POST http://localhost:8080/api/payments/create-session/82 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -v
```

---

**After rebuilding, the 500 error should be fixed!** 🎉

# 🔧 Rebuild Services to Fix 500 Errors

## ⚠️ IMPORTANT: Services Need Rebuild

The 500 errors are happening because the **services need to be recompiled** with the fixes we made.

## 🚀 Quick Fix (3 Steps)

### Step 1: Rebuild Payment Service

```bash
cd services/payment-service
mvn clean package -DskipTests
cd ../..
```

### Step 2: Rebuild Delivery Service

```bash
cd services/delivery-service
mvn clean package -DskipTests
cd ../..
```

### Step 3: Restart Services

**If using Docker:**
```bash
docker-compose restart payment-service delivery-service
```

**If running locally:**
- Stop the services (Ctrl+C)
- Restart them:
```bash
java -jar services/payment-service/target/payment-service-*.jar
java -jar services/delivery-service/target/delivery-service-*.jar
```

## ✅ Verify Fixes

After rebuild and restart:

1. **Check logs:**
```bash
# Payment service
docker-compose logs payment-service --tail 20

# Delivery service  
docker-compose logs delivery-service --tail 20
```

2. **Test endpoints:**
- `GET /api/payments/order/82` → Should return 200 (not 500)
- `GET /api/deliveries/order/82` → Should return 200 (not 500)
- `POST /api/payments/create-session/82` → Should return 200 (not 500)

## 🎯 Expected Behavior After Rebuild

### Payment Endpoint (`GET /api/payments/order/82`)
- ✅ Returns 200 OK
- ✅ Returns `{"status": "PENDING"}` if payment doesn't exist
- ❌ No longer returns 500

### Delivery Endpoint (`GET /api/deliveries/order/82`)
- ✅ Returns 200 OK
- ✅ Returns `null` if delivery not assigned
- ❌ No longer returns 500

### Create Session (`POST /api/payments/create-session/82`)
- ✅ Returns 200 OK with checkout URL
- ✅ Works in mock mode (default)
- ❌ No longer returns 500

## 🔍 If Still Getting 500 After Rebuild

1. **Check service logs for exact error:**
```bash
docker-compose logs payment-service | grep -i "error\|exception" | tail -20
```

2. **Verify services are registered in Eureka:**
- Go to: http://localhost:8761
- Check `payment-service` and `delivery-service` are listed

3. **Check API Gateway logs:**
```bash
docker-compose logs api-gateway --tail 30
```

## 📋 Quick Rebuild Script

Create `rebuild-services.sh`:

```bash
#!/bin/bash
echo "Rebuilding payment-service..."
cd services/payment-service && mvn clean package -DskipTests && cd ../..
echo "Rebuilding delivery-service..."
cd services/delivery-service && mvn clean package -DskipTests && cd ../..
echo "Done! Restart services now."
```

Run: `bash rebuild-services.sh` (or `./rebuild-services.sh` on Linux/Mac)

---

**After rebuilding, the 500 errors should be fixed!** 🎉

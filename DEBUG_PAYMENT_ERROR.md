# 🔍 Debug Payment 500 Error - Step by Step

## Quick Checks

### 1. Check if Services are Running

```bash
# Check all services
docker-compose ps

# Check payment-service logs
docker-compose logs payment-service --tail 100

# Check order-service logs  
docker-compose logs order-service --tail 50
```

### 2. Verify Mock Mode is Enabled

Check `docker-compose.yml`:
```yaml
payment-service:
  environment:
    STRIPE_MOCK_MODE: "true"  # Should be true for testing
```

Or check logs for:
```
Mock mode enabled: true
```

### 3. Check Order Service is Reachable

In payment-service logs, look for:
- `Failed to fetch order from order-service` → Order service not reachable
- `Order not found` → Order doesn't exist
- `Cannot reach order-service` → Eureka/Feign issue

### 4. Verify Eureka Registration

Check if services are registered:
- Go to: http://localhost:8761
- Look for `order-service` and `payment-service` in the list

## Common Issues & Fixes

### Issue 1: Mock Mode Not Enabled

**Symptom:** Logs show "REAL STRIPE MODE" instead of "MOCK MODE"

**Fix:**
```yaml
# docker-compose.yml
STRIPE_MOCK_MODE: "true"
```

Then restart:
```bash
docker-compose restart payment-service
```

### Issue 2: Order Service Not Reachable

**Symptom:** Error: "Cannot reach order-service" or "Failed to fetch order"

**Fix:**
1. Check order-service is running:
```bash
docker-compose ps order-service
```

2. Check Eureka registration:
- Go to http://localhost:8761
- Verify `order-service` is listed

3. Restart order-service:
```bash
docker-compose restart order-service
```

### Issue 3: Order Not Found

**Symptom:** Error: "Order not found: {orderId}"

**Fix:**
1. Verify order exists:
```bash
# Check order-service logs
docker-compose logs order-service | grep "orderId"
```

2. Check order status - should be `PENDING_PAYMENT` or `PLACED`

### Issue 4: Feign Client Error

**Symptom:** Connection timeout or 404 from order-service

**Fix:**
1. Check service names match:
   - Eureka service name: `order-service`
   - Feign client name: `order-service`

2. Check network:
```bash
docker-compose exec payment-service ping order-service
```

## Get Detailed Error

Add this to see exact error:

1. **Check browser console** - Look for error response
2. **Check payment-service logs:**
```bash
docker-compose logs payment-service | grep -i "error\|exception\|failed" | tail -20
```

3. **Enable debug logging:**
```yaml
# application.yml
logging:
  level:
    "[com.fooddelivery.payment]": DEBUG
    "[feign]": DEBUG
```

## Quick Test

Test the endpoint directly:

```bash
# Replace {orderId} with actual order ID and {token} with JWT token
curl -X POST http://localhost:8080/api/payments/create-session/82 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

Check the response and error message.

## Still Not Working?

Send me:
1. Payment service logs (last 50 lines)
2. Order service logs (last 20 lines)
3. Browser console error
4. The exact error message from the API response

I'll pinpoint the exact issue! 🔍

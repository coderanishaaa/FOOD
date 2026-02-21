# 🔧 Eureka Registration Troubleshooting Guide

## 🚨 Common Errors & Solutions

### Error 1: "Connection refused http://localhost:8761/eureka"

**Cause:** Service trying to connect to `localhost` in Docker environment

**Solution:**
1. **Check environment variable:**
```bash
docker-compose exec payment-service env | grep EUREKA_URL
```
Should show: `EUREKA_URL=http://eureka-server:8761/eureka/`

2. **Verify Eureka server is running:**
```bash
docker-compose ps eureka-server
```

3. **Check network connectivity:**
```bash
docker-compose exec payment-service ping eureka-server
```

### Error 2: "DiscoveryClient was unable to send heartbeat"

**Cause:** Service registered but can't maintain connection

**Solutions:**
1. **Check health endpoint:**
```bash
curl http://localhost:8084/actuator/health
```
Should return: `{"status":"UP"}`

2. **Verify network:**
```bash
docker-compose exec payment-service curl http://eureka-server:8761/eureka/
```

3. **Check lease settings:**
- `lease-renewal-interval-in-seconds: 10` (heartbeat frequency)
- `lease-expiration-duration-in-seconds: 30` (timeout)

### Error 3: Service Shows "DOWN" in Eureka Dashboard

**Cause:** Health check endpoint not accessible

**Solutions:**
1. **Verify actuator is enabled:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

2. **Test health endpoint:**
```bash
curl http://localhost:8084/actuator/health
```

3. **Check hostname/IP configuration:**
- In Docker: `prefer-ip-address: true`
- Health URL should be accessible from Eureka server

### Error 4: Service Doesn't Appear in Eureka Dashboard

**Cause:** Registration failed silently

**Solutions:**
1. **Check service logs:**
```bash
docker-compose logs payment-service | grep -i "eureka\|discovery\|register"
```

2. **Verify configuration:**
```yaml
eureka:
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
```

3. **Check service name:**
```yaml
spring:
  application:
    name: payment-service
```

## 🔍 Diagnostic Commands

### 1. Check Eureka Server Status
```bash
# Docker
docker-compose logs eureka-server --tail 50

# Local
curl http://localhost:8761
```

### 2. Check Payment Service Logs
```bash
docker-compose logs payment-service | grep -i eureka
```

**Expected output:**
```
DiscoveryClient_PAYMENT-SERVICE - Registering application PAYMENT-SERVICE
DiscoveryClient_PAYMENT-SERVICE - registration status: 204
DiscoveryClient_PAYMENT-SERVICE - Registered application PAYMENT-SERVICE
```

### 3. Verify Network Connectivity
```bash
# From payment-service container
docker-compose exec payment-service ping eureka-server

# Test Eureka endpoint
docker-compose exec payment-service curl http://eureka-server:8761/eureka/
```

### 4. Check Service Registration
```bash
# View Eureka dashboard
open http://localhost:8761

# Or via API
curl http://localhost:8761/eureka/apps/PAYMENT-SERVICE
```

### 5. Verify Environment Variables
```bash
docker-compose exec payment-service env | grep -i eureka
```

**Expected:**
```
EUREKA_URL=http://eureka-server:8761/eureka/
```

## 📋 Step-by-Step Verification

### Step 1: Verify Eureka Server
```bash
# Check if running
docker-compose ps eureka-server

# Check logs
docker-compose logs eureka-server --tail 20

# Test endpoint
curl http://localhost:8761
```

### Step 2: Verify Payment Service Configuration
```bash
# Check environment
docker-compose exec payment-service env | grep EUREKA_URL

# Check application.yml
docker-compose exec payment-service cat /app/application.yml | grep -A 10 eureka
```

### Step 3: Check Service Logs
```bash
# Look for registration attempts
docker-compose logs payment-service | grep -i "register\|eureka\|discovery"

# Look for errors
docker-compose logs payment-service | grep -i "error\|exception\|failed"
```

### Step 4: Verify Health Endpoints
```bash
# Health check
curl http://localhost:8084/actuator/health

# Info endpoint
curl http://localhost:8084/actuator/info
```

### Step 5: Check Eureka Dashboard
1. Open: http://localhost:8761
2. Look for "Instances currently registered with Eureka"
3. Find `PAYMENT-SERVICE`
4. Status should be "UP" (green)

## 🛠️ Quick Fixes

### Fix 1: Restart Services
```bash
docker-compose restart eureka-server payment-service
```

### Fix 2: Rebuild Service
```bash
docker-compose up --build -d payment-service
```

### Fix 3: Check Network
```bash
# Verify services are on same network
docker network inspect food_food-network | grep -A 5 payment-service
docker network inspect food_food-network | grep -A 5 eureka-server
```

### Fix 4: Force Re-registration
```bash
# Restart payment service
docker-compose restart payment-service

# Wait 30 seconds
sleep 30

# Check registration
curl http://localhost:8761/eureka/apps/PAYMENT-SERVICE
```

## 🎯 Expected Behavior

### Successful Registration:

1. **Service starts:**
```
Started PaymentServiceApplication in X seconds
```

2. **Eureka client initializes:**
```
DiscoveryClient_PAYMENT-SERVICE - Initializing Eureka client
```

3. **Registration attempt:**
```
DiscoveryClient_PAYMENT-SERVICE - Registering application PAYMENT-SERVICE
```

4. **Registration success:**
```
DiscoveryClient_PAYMENT-SERVICE - registration status: 204
DiscoveryClient_PAYMENT-SERVICE - Registered application PAYMENT-SERVICE
```

5. **Heartbeat starts:**
```
DiscoveryClient_PAYMENT-SERVICE - Renewing lease
```

### In Eureka Dashboard:

- ✅ Service appears in "Instances currently registered with Eureka"
- ✅ Status shows "UP" (green)
- ✅ Health check URL is accessible
- ✅ Last heartbeat is recent (< 30 seconds)

## 🔒 Production Checklist

- [ ] Eureka server is highly available (multiple instances)
- [ ] Self-preservation is enabled
- [ ] Proper timeouts configured
- [ ] Health checks are working
- [ ] Monitoring alerts are set up
- [ ] Network is properly configured
- [ ] Service discovery is tested

---

**If issues persist, check the detailed logs and share the error messages!** 🔍

# ✅ Eureka Registration Fix - Complete Solution

## 🔍 Root Cause Analysis

### Why "Connection refused http://localhost:8761/eureka" Happens:

1. **Docker Network Issue**: In Docker, services can't use `localhost` - they must use service names
2. **Hostname Resolution**: Docker containers resolve service names via Docker's internal DNS
3. **Network Isolation**: Each container has its own network namespace
4. **Timing Issue**: Service tries to register before Eureka is ready

### The Problem:
- **Local Run**: `localhost:8761` works ✅
- **Docker Run**: `localhost:8761` fails ❌ (should use `eureka-server:8761`)

## ✅ Fixes Applied

### 1. Enhanced Eureka Configuration

**Updated `application.yml`:**
- ✅ Proper hostname/IP configuration
- ✅ Health check endpoints
- ✅ Retry logic
- ✅ Lease renewal settings
- ✅ Environment variable support

### 2. Added Actuator Dependency

**Updated `pom.xml`:**
- ✅ Added `spring-boot-starter-actuator`
- ✅ Enables health check endpoints
- ✅ Required for Eureka health monitoring

### 3. Environment Variable Support

**Docker Compose:**
- ✅ `EUREKA_URL` environment variable
- ✅ Automatically set to `http://eureka-server:8761/eureka/` in Docker
- ✅ Falls back to `http://localhost:8761/eureka/` locally

## 📋 Configuration Details

### Local Run Configuration

When running locally, Eureka URL defaults to:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Docker Run Configuration

In Docker, environment variable is set:
```yaml
environment:
  EUREKA_URL: http://eureka-server:8761/eureka/
```

The service automatically uses the correct URL based on environment.

## 🚀 How to Use

### Local Development

1. **Start Eureka Server:**
```bash
java -jar infrastructure/eureka-server/target/eureka-server-*.jar
```

2. **Start Payment Service:**
```bash
java -jar services/payment-service/target/payment-service-*.jar
```

3. **Verify Registration:**
- Go to: http://localhost:8761
- Look for `PAYMENT-SERVICE` in the list

### Docker Run

1. **Start All Services:**
```bash
docker-compose up -d
```

2. **Verify Registration:**
- Go to: http://localhost:8761
- Look for `PAYMENT-SERVICE` in the list

## 🔧 Troubleshooting

### Issue 1: Still Getting "Connection refused"

**Check:**
1. Is Eureka server running?
```bash
# Docker
docker-compose ps eureka-server

# Local
curl http://localhost:8761
```

2. Is the URL correct?
```bash
# Check environment variable
docker-compose exec payment-service env | grep EUREKA_URL
```

3. Check service logs:
```bash
docker-compose logs payment-service | grep -i eureka
```

### Issue 2: Service Registers but Shows "DOWN"

**Fix:**
- Health check endpoint must be accessible
- Verify: `http://localhost:8084/actuator/health`
- Should return: `{"status":"UP"}`

### Issue 3: Service Doesn't Appear in Eureka Dashboard

**Check:**
1. Service name matches: `payment-service`
2. Eureka client is enabled: `eureka.client.enabled=true`
3. Registration is enabled: `eureka.client.register-with-eureka=true`

### Issue 4: Intermittent Registration Failures

**Fix:**
- Increase retry intervals in `application.yml`
- Check network connectivity
- Verify Eureka server is stable

## 📊 Verification Steps

### Step 1: Check Service Logs

```bash
docker-compose logs payment-service | grep -i "eureka\|discovery"
```

**Expected output:**
```
DiscoveryClient_PAYMENT-SERVICE - registration status: 204
DiscoveryClient_PAYMENT-SERVICE - Registered application PAYMENT-SERVICE
```

### Step 2: Check Eureka Dashboard

1. Go to: http://localhost:8761
2. Look for "Instances currently registered with Eureka"
3. Find `PAYMENT-SERVICE`
4. Status should be "UP" (green)

### Step 3: Test Service Discovery

```bash
# From another service, test Feign client
# Should be able to call: lb://payment-service
```

## 🎯 Expected Behavior

### After Fix:

✅ **Service starts successfully**
✅ **Connects to Eureka within 10 seconds**
✅ **Appears in Eureka dashboard**
✅ **Status shows "UP"**
✅ **Health checks pass**
✅ **Other services can discover it**

### Logs Should Show:

```
DiscoveryClient_PAYMENT-SERVICE - Registering application PAYMENT-SERVICE
DiscoveryClient_PAYMENT-SERVICE - registration status: 204
DiscoveryClient_PAYMENT-SERVICE - Registered application PAYMENT-SERVICE
```

## 🔒 Production Recommendations

1. **Enable Self-Preservation** in Eureka server
2. **Use Multiple Eureka Servers** for high availability
3. **Configure Proper Timeouts**
4. **Monitor Registration Status**
5. **Set Up Alerts** for registration failures

---

## ✅ Files Modified

1. ✅ `services/payment-service/pom.xml` - Added actuator
2. ✅ `services/payment-service/src/main/resources/application.yml` - Enhanced Eureka config
3. ✅ `services/payment-service/src/main/java/com/fooddelivery/payment/config/EurekaConfig.java` - NEW

---

**The service should now register successfully with Eureka!** 🎉

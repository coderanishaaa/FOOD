# Food Delivery System — Microservices Architecture

A production-ready food delivery platform built with Spring Boot 3.x microservices, React.js frontend, Apache Kafka, and MySQL.

## Architecture

```
Frontend (React/Vite :3000)
    ↓
API Gateway (:8080) — JWT validation, routing
    ↓
Eureka Server (:8761) — Service Discovery
    ↓
┌──────────────┬──────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
│ user-service │ restaurant-  │ order-service│ payment-     │ delivery-    │ notification-│
│ :8081        │ service :8082│ :8083        │ service :8084│ service :8085│ service :8086│
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
    ↓               ↓              ↓ Kafka          ↓ Kafka        ↓ Kafka        ↓ Kafka
  MySQL           MySQL          MySQL            MySQL          MySQL          MySQL
 (user_db)    (restaurant_db)  (order_db)      (payment_db)  (delivery_db) (notification_db)
```

**Event Flow:** Order Placed → `order-created-topic` → Payment Service (creates payment record) → Customer pays via Stripe → Webhook → `payment-success-topic` → Delivery Service → `delivery-update-topic` → Notification Service (consumes all topics)

**Payment Flow:** Order with `PENDING_PAYMENT` → Stripe Checkout Session → Customer pays → Stripe Webhook → Order updated to `PAID` → Delivery assigned automatically

## Prerequisites

- **Java 17** (JDK)
- **Maven 3.8+**
- **Node.js 18+** (for frontend)
- **MySQL 8.0** (local) or Docker
- **Apache Kafka** (local) or Docker
- **Docker & Docker Compose** (for containerized run)
- **Stripe Account** (for payment integration) - See [STRIPE_SETUP.md](STRIPE_SETUP.md)

---

## Option 1: Local Run

### 1. Start MySQL

Ensure MySQL is running on `localhost:3306` with user `root` / password `root`. Create the databases:

```sql
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS restaurant_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS delivery_db;
CREATE DATABASE IF NOT EXISTS notification_db;
```

### 2. Start Kafka & Zookeeper

Start Zookeeper and Kafka on default ports (2181 and 9092).

### 3. Build All Services

From the project root:

```bash
mvn clean package -DskipTests
```

### 4. Start Services (in order)

```bash
# Terminal 1 — Eureka Server
java -jar infrastructure/eureka-server/target/*.jar

# Terminal 2 — API Gateway
java -jar infrastructure/api-gateway/target/*.jar

# Terminal 3 — User Service
java -jar services/user-service/target/*.jar

# Terminal 4 — Restaurant Service
java -jar services/restaurant-service/target/*.jar

# Terminal 5 — Order Service
java -jar services/order-service/target/*.jar

# Terminal 6 — Payment Service
java -jar services/payment-service/target/*.jar

# Terminal 7 — Delivery Service
java -jar services/delivery-service/target/*.jar

# Terminal 8 — Notification Service
java -jar services/notification-service/target/*.jar
```

### 5. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

### 6. Access

- **Frontend:** http://localhost:3000
- **API Gateway:** http://localhost:8080
- **Eureka Dashboard:** http://localhost:8761

---

## Option 2: Docker Run

### 1. Build All Services

```bash
mvn clean package -DskipTests
```

### 2. Start Everything

```bash
docker-compose up --build -d
```

### 3. Access

- **Frontend:** http://localhost:3000
- **API Gateway:** http://localhost:8080
- **Eureka Dashboard:** http://localhost:8761

### Stop

```bash
docker-compose down
```

### Stop + Remove Volumes

```bash
docker-compose down -v
```

---

## Postman Collection

Import `postman/Food-Delivery-System.postman_collection.json` into Postman.

**Test Flow:**
1. Register a Restaurant Owner → Login
2. Create a Restaurant → Add Menu Items
3. Login as Customer
4. Place an Order (status: `PENDING_PAYMENT`)
5. Click "Proceed to Payment" → Complete Stripe Checkout
6. Order status updates to `PAID` → Delivery assigned automatically
7. Check Payment, Delivery, and Notification endpoints
8. Login as Delivery Agent → Update delivery status

---

## Service Ports

| Service             | Port  |
|---------------------|-------|
| Eureka Server       | 8761  |
| API Gateway         | 8080  |
| User Service        | 8081  |
| Restaurant Service  | 8082  |
| Order Service       | 8083  |
| Payment Service     | 8084  |
| Delivery Service    | 8085  |
| Notification Service| 8086  |
| Frontend            | 3000  |
| MySQL               | 3306  |
| Kafka               | 9092  |
| Zookeeper           | 2181  |

## Kafka Topics

| Topic                 | Producer         | Consumers                         |
|-----------------------|------------------|-----------------------------------|
| order-created-topic   | order-service    | payment-service, notification-service |
| payment-success-topic | payment-service  | delivery-service, notification-service |
| delivery-update-topic | delivery-service | notification-service              |
| notification-topic    | all services     | notification-service              |

## Stripe Payment Integration

The system includes **production-ready Stripe payment integration**:

- ✅ Stripe Checkout Session creation
- ✅ Webhook handling with signature verification
- ✅ Automatic order status updates
- ✅ Payment button visibility fix
- ✅ Complete payment flow: Order → Payment → Delivery

**Setup Instructions:** See [STRIPE_SETUP.md](STRIPE_SETUP.md) for detailed configuration and testing guide.

## User Roles

- **CUSTOMER** — Browse restaurants, place orders, track deliveries
- **RESTAURANT_OWNER** — Manage restaurants and menu items, view orders
- **DELIVERY_AGENT** — View assigned deliveries, update delivery status
- **ADMIN** — View all notifications and restaurants

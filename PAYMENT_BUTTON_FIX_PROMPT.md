# ✅ PROMPT — Fix Payment Button Not Showing + Complete Payment Flow

You are a **Senior Full-Stack Microservices Engineer**.

I already have a **Food Delivery Microservices System** built with:

* Spring Boot microservices
* API Gateway
* Eureka
* JWT authentication
* React frontend
* Order tracking page
* **Stripe payment integration** (already implemented)

But I have a bug:

> After placing an order, the **payment button is not visible** on the tracking page.

Currently order status is:

```text
PENDING_PAYMENT
```

And tracking timeline should show **Payment button** when status is `PENDING_PAYMENT`.

---

# 🎯 YOUR TASK

Fix the system so the complete flow works correctly:

> Place Order → Payment Button Visible on Tracking Page → Payment → Delivery → Tracking Updates

---

# ✅ REQUIRED FIXES

## 1️⃣ Backend — Order Service (VERIFY)

Ensure when creating order, status is set to:

```java
PENDING_PAYMENT
```

NOT `PLACED`.

Verify enum includes:

```java
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
```

Return status in API response.

---

## 2️⃣ Frontend — Tracking Page Payment Button

On **OrderTracking page** (`/track/:orderId`), show payment button when:

```javascript
order.status === "PENDING_PAYMENT"
```

Add button in Order Details card:

```jsx
{order.status === 'PENDING_PAYMENT' && (
  <button
    className="btn btn-success"
    onClick={handlePayment}
    disabled={loading}
  >
    {loading ? 'Processing...' : '💳 Proceed to Payment'}
  </button>
)}
```

When clicked:

Call:

```http
POST /api/payments/create-session/{orderId}
```

Then redirect user to payment URL:

```javascript
window.location.href = checkoutUrl;
```

---

## 3️⃣ Payment Service Integration (VERIFY)

Ensure endpoint exists:

```http
POST /api/payments/create-session/{orderId}
```

Returns:

```json
{
  "success": true,
  "data": {
    "sessionId": "cs_test_...",
    "url": "https://checkout.stripe.com/..."
  }
}
```

After webhook success:

Update order:

```java
orderStatus = PAID
```

---

## 4️⃣ Tracking Timeline Fix

Map statuses correctly in timeline:

```javascript
const stepMap = {
  PENDING_PAYMENT: 1,
  PAID: 2,
  ASSIGNED: 3,
  OUT_FOR_DELIVERY: 4,
  DELIVERED: 5
};

const steps = [
  { label: 'Order Placed', done: currentStep >= 1, active: currentStep === 1 },
  { label: 'Payment Done', done: currentStep >= 2, active: currentStep === 2 },
  { label: 'Agent Assigned', done: currentStep >= 3, active: currentStep === 3 },
  { label: 'Out for Delivery', done: currentStep >= 4, active: currentStep === 4 },
  { label: 'Delivered', done: currentStep >= 5, active: currentStep === 5 },
];
```

---

## 5️⃣ Delivery Flow (VERIFY)

After payment success:

* Payment service publishes Kafka event to `payment-success-topic`
* Delivery service consumes event
* Delivery service assigns agent
* Order status updated to `ASSIGNED`

---

## 6️⃣ Required Improvements

Implement:

✅ Payment button in OrderTracking page Order Details card
✅ Payment section card when order is PENDING_PAYMENT and no payment record exists
✅ Payment success message
✅ Auto refresh order status (polling every 10 seconds)
✅ Disable button after payment initiated
✅ Error handling with user-friendly messages
✅ Loading state during payment session creation
✅ Visual timeline with active step highlighting

---

# ✅ DATABASE FIX (If Needed)

If existing orders have status `PLACED`, convert them:

```sql
UPDATE orders
SET status = 'PENDING_PAYMENT'
WHERE status = 'PLACED';
```

---

# ✅ EXPECTED RESULT

After fixes:

1. User places order → Status: `PENDING_PAYMENT`
2. User navigates to tracking page (`/track/{orderId}`)
3. Tracking page shows **"Proceed to Payment"** button in Order Details card
4. User clicks payment button
5. Redirected to Stripe Checkout
6. Payment success → Webhook received
7. Status becomes **PAID**
8. Delivery assigned automatically
9. Timeline updates in real-time
10. Status progresses: `PAID` → `ASSIGNED` → `OUT_FOR_DELIVERY` → `DELIVERED`

No broken states allowed.

---

# ✅ OUTPUT FORMAT

Provide:

1. Updated `OrderTracking.jsx` with payment button
2. Updated timeline logic with status mapping
3. Payment handler function
4. Error handling
5. Loading states
6. Any CSS/styling improvements

Do NOT skip files.

---

# ✅ FILES TO UPDATE

1. `frontend/src/pages/OrderTracking.jsx`
   - Add payment button handler
   - Update timeline status mapping
   - Add payment section card
   - Improve visual design

---

# ✅ TESTING CHECKLIST

- [ ] Order created with `PENDING_PAYMENT` status
- [ ] Tracking page shows payment button
- [ ] Payment button click creates Stripe session
- [ ] Redirect to Stripe works
- [ ] After payment, status updates to `PAID`
- [ ] Timeline shows correct active step
- [ ] Delivery assigned automatically
- [ ] Timeline updates in real-time
- [ ] Error messages display correctly
- [ ] Loading states work properly

---

# 🚀 END PROMPT

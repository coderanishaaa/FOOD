import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import API from '../api/axios';

/**
 * Displays real-time tracking info for a single order.
 * Fetches order details, payment status, and delivery status.
 */
export default function OrderTracking() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [delivery, setDelivery] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTrackingData();
    // Poll every 10 seconds for status updates
    const interval = setInterval(fetchTrackingData, 10000);
    return () => clearInterval(interval);
  }, [orderId]);

  const fetchTrackingData = async () => {
    try {
      const orderRes = await API.get(`/api/orders/${orderId}`);
      setOrder(orderRes.data.data);
    } catch { /* order not found */ }

    try {
      const payRes = await API.get(`/api/payments/order/${orderId}`);
      setPayment(payRes.data.data);
    } catch { /* payment not yet created */ }

    try {
      const delRes = await API.get(`/api/deliveries/order/${orderId}`);
      setDelivery(delRes.data.data);
    } catch { /* delivery not yet assigned */ }
  };

  const steps = [
    { label: 'Order Placed', done: !!order },
    { label: 'Payment Processed', done: payment?.status === 'COMPLETED' },
    { label: 'Delivery Assigned', done: !!delivery },
    { label: 'Picked Up', done: delivery?.status === 'PICKED_UP' || delivery?.status === 'DELIVERED' },
    { label: 'Delivered', done: delivery?.status === 'DELIVERED' },
  ];

  return (
    <div className="container">
      <Link to="/customer/orders" className="btn btn-secondary btn-sm">&larr; Back to Orders</Link>
      <h2 style={{ marginTop: 16 }}>Order #{orderId} — Tracking</h2>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Progress Tracker */}
      <div className="card" style={{ marginTop: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          {steps.map((step, i) => (
            <div key={i} style={{ textAlign: 'center', flex: 1 }}>
              <div style={{
                width: 32, height: 32, borderRadius: '50%', margin: '0 auto 8px',
                background: step.done ? '#27ae60' : '#ddd', color: 'white',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontWeight: 'bold', fontSize: '0.8rem',
              }}>
                {step.done ? '✓' : i + 1}
              </div>
              <span style={{ fontSize: '0.8rem', color: step.done ? '#27ae60' : '#888' }}>
                {step.label}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Order Details */}
      {order && (
        <div className="card">
          <h3>Order Details</h3>
          <p>Status: <strong>{order.status}</strong></p>
          <p>Total: <strong>${Number(order.totalAmount).toFixed(2)}</strong></p>
          <p>Address: {order.deliveryAddress}</p>
          {order.items?.map((item, idx) => (
            <div key={idx} className="order-item">
              <span>{item.menuItemName} x{item.quantity}</span>
              <span>${(item.price * item.quantity).toFixed(2)}</span>
            </div>
          ))}
        </div>
      )}

      {/* Payment Details */}
      {payment && (
        <div className="card">
          <h3>Payment</h3>
          <p>Status: <strong>{payment.status}</strong></p>
          <p>Amount: ${Number(payment.amount).toFixed(2)}</p>
        </div>
      )}

      {/* Delivery Details */}
      {delivery && (
        <div className="card">
          <h3>Delivery</h3>
          <p>Status: <strong>{delivery.status}</strong></p>
          <p>Delivery Agent ID: {delivery.deliveryAgentId}</p>
        </div>
      )}
    </div>
  );
}

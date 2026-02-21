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
  const [loading, setLoading] = useState(false);

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

  const handlePayment = async () => {
    if (!order) return;
    setLoading(true);
    setError('');
    try {
      const res = await API.post(`/api/payments/create-session/${orderId}`);
      const checkoutUrl = res.data.data?.url;

      if (checkoutUrl) {
        // Redirect to Stripe Checkout
        window.location.href = checkoutUrl;
      } else {
        setError('Failed to create payment session');
        setLoading(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to initiate payment');
      setLoading(false);
    }
  };

  const handleRazorpayPayment = async () => {
    if (!order) return;
    setLoading(true);
    setError('');

    // Load Razorpay Script
    const loadScript = () => {
      return new Promise((resolve) => {
        const script = document.createElement('script');
        script.src = 'https://checkout.razorpay.com/v1/checkout.js';
        script.onload = () => resolve(true);
        script.onerror = () => resolve(false);
        document.body.appendChild(script);
      });
    };

    const resScript = await loadScript();
    if (!resScript) {
      setError('Razorpay SDK failed to load.');
      setLoading(false);
      return;
    }

    try {
      const res = await API.post(`/api/payments/create-razorpay-session/${orderId}`);
      const rzpOrder = res.data.data;

      if (!rzpOrder || !rzpOrder.orderId) {
        setError('Failed to create Razorpay Order');
        setLoading(false);
        return;
      }

      const options = {
        key: rzpOrder.keyId || 'rzp_test_mock_key_123',
        amount: Math.round(rzpOrder.amount * 100),
        currency: rzpOrder.currency || 'INR',
        name: 'Food Delivery System',
        description: `Order #${orderId}`,
        order_id: rzpOrder.orderId,
        handler: async function (response) {
          try {
            await API.post(`/api/payments/razorpay-complete/${orderId}`, {
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_order_id: response.razorpay_order_id,
              razorpay_signature: response.razorpay_signature
            });
            fetchTrackingData();
          } catch (err) {
            setError('Payment verification failed');
          }
        },
        prefill: {
          name: rzpOrder.customerName || 'Customer',
          email: rzpOrder.customerEmail || 'customer@example.com',
          contact: '9999999999'
        },
        theme: {
          color: '#3399cc'
        }
      };

      const rzp1 = new window.Razorpay(options);
      rzp1.on('payment.failed', function (response) {
        setError(`Payment failed: ${response.error.description}`);
      });
      rzp1.open();
      setLoading(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to initiate Razorpay payment');
      setLoading(false);
    }
  };

  // Map order status to timeline steps
  const getStepStatus = () => {
    if (!order) return { currentStep: 0, steps: [] };

    const stepMap = {
      PENDING_PAYMENT: 1,
      PLACED: 1,  // Treat PLACED same as PENDING_PAYMENT for backward compatibility
      PAID: 2,
      ASSIGNED: 3,
      OUT_FOR_DELIVERY: 4,
      DELIVERED: 5,
      CANCELLED: 0
    };

    const currentStep = stepMap[order.status] || 0;

    const steps = [
      { label: 'Order Placed', done: currentStep >= 1, active: currentStep === 1 },
      { label: 'Payment Done', done: currentStep >= 2, active: currentStep === 2 },
      { label: 'Agent Assigned', done: currentStep >= 3, active: currentStep === 3 },
      { label: 'Out for Delivery', done: currentStep >= 4, active: currentStep === 4 },
      { label: 'Delivered', done: currentStep >= 5, active: currentStep === 5 },
    ];

    return { currentStep, steps };
  };

  const { steps } = getStepStatus();

  return (
    <div className="container">
      <Link to="/customer/orders" className="btn btn-secondary btn-sm">&larr; Back to Orders</Link>
      <h2 style={{ marginTop: 16 }}>Order #{orderId} — Tracking</h2>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Progress Tracker */}
      <div className="card" style={{ marginTop: 16, padding: '24px' }}>
        <h3 style={{ marginBottom: 24 }}>Order Progress</h3>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', position: 'relative' }}>
          {/* Connector lines */}
          <div style={{
            position: 'absolute',
            top: '20px',
            left: '5%',
            right: '5%',
            height: '2px',
            background: '#ddd',
            zIndex: 0
          }} />
          {steps.map((step, i) => {
            const completedSteps = steps.filter((s, idx) => idx < i && s.done).length;
            const connectorWidth = `${100 / (steps.length - 1)}%`;
            return (
              <div key={i} style={{ textAlign: 'center', flex: 1, position: 'relative', zIndex: 1 }}>
                <div style={{
                  width: 40, height: 40, borderRadius: '50%', margin: '0 auto 8px',
                  background: step.done ? '#27ae60' : step.active ? '#3498db' : '#ddd',
                  color: 'white',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontWeight: 'bold', fontSize: '0.9rem',
                  border: step.active ? '3px solid #2980b9' : 'none',
                  transition: 'all 0.3s ease',
                  boxShadow: step.active ? '0 0 10px rgba(52, 152, 219, 0.5)' : 'none'
                }}>
                  {step.done ? '✓' : i + 1}
                </div>
                <span style={{
                  fontSize: '0.85rem',
                  color: step.done ? '#27ae60' : step.active ? '#3498db' : '#888',
                  fontWeight: step.active ? 'bold' : 'normal'
                }}>
                  {step.label}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Order Details */}
      {order && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3>Order Details</h3>
            <span className={`badge ${order.status === 'PENDING_PAYMENT' || order.status === 'PLACED' ? 'badge-pending' :
              order.status === 'PAID' ? 'badge-confirmed' :
                order.status === 'DELIVERED' ? 'badge-delivered' : 'badge-assigned'}`}>
              {order.status}
            </span>
          </div>
          <p>Total: <strong>${Number(order.totalAmount).toFixed(2)}</strong></p>
          <p>Address: {order.deliveryAddress}</p>
          {order.items?.map((item, idx) => (
            <div key={idx} className="order-item">
              <span>{item.menuItemName} x{item.quantity}</span>
              <span>${(item.price * item.quantity).toFixed(2)}</span>
            </div>
          ))}

          {/* Payment Button - Show for both PENDING_PAYMENT and PLACED (backward compatibility) */}
          {(order.status === 'PENDING_PAYMENT' || order.status === 'PLACED') && (
            <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #eee' }}>
              <button
                className="btn btn-success"
                onClick={handlePayment}
                disabled={loading}
                style={{ width: '100%', marginBottom: 8 }}
              >
                {loading ? 'Processing...' : '💳 Pay with Stripe'}
              </button>
              <button
                className="btn btn-primary"
                onClick={handleRazorpayPayment}
                disabled={loading}
                style={{ width: '100%' }}
              >
                {loading ? 'Processing...' : '⚡ Pay with Razorpay (Test Mode)'}
              </button>
              {error && <div className="alert alert-error" style={{ marginTop: 8 }}>{error}</div>}
            </div>
          )}
        </div>
      )}

      {/* Payment Details */}
      {payment && (
        <div className="card">
          <h3>Payment</h3>
          <p>Status: <strong style={{
            color: payment.status === 'COMPLETED' ? '#27ae60' :
              payment.status === 'PENDING' ? '#f39c12' : '#e74c3c'
          }}>{payment.status}</strong></p>
          <p>Amount: <strong>${Number(payment.amount).toFixed(2)}</strong></p>
          {payment.transactionId && (
            <p style={{ fontSize: '0.85rem', color: '#888' }}>
              Transaction ID: {payment.transactionId}
            </p>
          )}
        </div>
      )}

      {/* Payment Section for PENDING_PAYMENT/PLACED orders without payment record yet */}
      {order && (order.status === 'PENDING_PAYMENT' || order.status === 'PLACED') && !payment && (
        <div className="card" style={{ background: '#fff9e6', border: '1px solid #f39c12' }}>
          <h3>💳 Payment Required</h3>
          <p>Complete payment to proceed with your order.</p>
          <button
            className="btn btn-success"
            onClick={handlePayment}
            disabled={loading}
            style={{ marginTop: 8, marginRight: 8 }}
          >
            {loading ? 'Processing...' : 'Pay with Stripe'}
          </button>
          <button
            className="btn btn-primary"
            onClick={handleRazorpayPayment}
            disabled={loading}
            style={{ marginTop: 8 }}
          >
            {loading ? 'Processing...' : 'Pay with Razorpay (Test Mode)'}
          </button>
          {error && <div className="alert alert-error" style={{ marginTop: 8 }}>{error}</div>}
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

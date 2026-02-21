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

  const handleOrderReceived = async () => {
    try {
      await API.put(`/api/orders/${orderId}/status?status=DELIVERED`);
      fetchTrackingData();
    } catch (err) { setError('Failed to confirm receipt'); }
  };

  // Map order status to timeline steps
  const getStepStatus = () => {
    if (!order) return { currentStep: 0, steps: [] };

    let progress = 0;
    switch (order.status) {
      case 'PENDING_RESTAURANT_CONFIRMATION': progress = 1; break;
      case 'PENDING_PAYMENT':
      case 'PLACED': progress = 2; break;
      case 'PAID': progress = 3; break;
      case 'ASSIGNED': progress = 4; break;
      case 'OUT_FOR_DELIVERY': progress = 5; break;
      case 'DELIVERED': progress = 6; break;
      case 'CANCELLED':
      case 'CANCELLED_BY_RESTAURANT': progress = -1; break;
      default: progress = 0; break;
    }

    const steps = [
      { label: progress > 1 ? 'Order Confirmed' : 'Order Placed', done: progress > 1, active: progress === 1 },
      { label: progress > 2 ? 'Payment Done' : 'Pending Payment', done: progress > 2, active: progress === 2 },
      { label: progress > 3 ? 'Agent Assigned' : 'Awaiting Agent', done: progress > 3, active: progress === 3 },
      { label: progress > 4 ? 'Out for Delivery' : 'Preparing Order', done: progress > 4, active: progress === 4 },
      { label: progress > 5 ? 'Delivered' : 'On The Way', done: progress > 5, active: progress === 5 },
    ];

    return { currentStep: progress, steps };
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
            <span className={`badge ${['PENDING_RESTAURANT_CONFIRMATION', 'PENDING_PAYMENT', 'PLACED'].includes(order.status) ? 'badge-pending' :
              order.status === 'PAID' ? 'badge-confirmed' :
                order.status === 'DELIVERED' ? 'badge-delivered' :
                  order.status.includes('CANCELLED') ? 'badge-error' : 'badge-assigned'}`}>
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

          {/* Restaurant Verification / Cancellation */}
          {order.status === 'PENDING_RESTAURANT_CONFIRMATION' && (
            <div style={{ marginTop: 16, padding: 16, background: '#fcf3cf', borderRadius: 8 }}>
              <p style={{ margin: 0 }}><strong>⏳ Wait for a quick check from the restaurant...</strong></p>
            </div>
          )}

          {order.status === 'CANCELLED_BY_RESTAURANT' && (
            <div style={{ marginTop: 16, padding: 16, background: '#f5b7b1', borderRadius: 8 }}>
              <h4 style={{ margin: '0 0 8px 0' }}>❌ Order Cancelled</h4>
              <p>The dish is unavailable right now... please choose something else.</p>
              <Link to="/customer/restaurants" className="btn btn-primary btn-sm" style={{ display: 'inline-block', marginTop: 10 }}>Browse Menu</Link>
            </div>
          )}

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

          {delivery.status === 'PENDING' ? (
            <div style={{ marginTop: 16, padding: 16, background: '#e8f8f5', borderRadius: 8 }}>
              <p style={{ margin: 0 }}><strong>⏳ Waiting for agent...</strong></p>
            </div>
          ) : (
            <>
              <p>Delivery Agent ID: {delivery.deliveryAgentId}</p>
              <div style={{ marginTop: 16, padding: 16, background: '#d5f5e3', borderRadius: 8 }}>
                <p style={{ margin: 0, color: '#145a32' }}><strong>🚗 Your delivery agent is on the way!</strong></p>
              </div>

              {/* Order Received Button */}
              {order?.status !== 'DELIVERED' && (
                <button className="btn btn-success" onClick={handleOrderReceived} style={{ marginTop: 16, width: '100%' }}>
                  Confirm Order Received
                </button>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}

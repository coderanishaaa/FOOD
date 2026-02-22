import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../api/axios';

export default function CustomerOrders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState({});

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const res = await API.get('/api/orders/my-orders');
      setOrders(res.data.data || []);
    } catch (err) {
      setError('Failed to load orders');
    }
  };

  const handleRazorpayPayment = async (orderId) => {
    setLoading((prev) => ({ ...prev, [orderId]: true }));
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
      setLoading((prev) => ({ ...prev, [orderId]: false }));
      return;
    }

    try {
      const res = await API.post(`/api/payments/create-razorpay-session/${orderId}`);
      const rzpOrder = res.data.data;

      if (!rzpOrder || !rzpOrder.orderId) {
        setError('Failed to create Razorpay Order');
        setLoading((prev) => ({ ...prev, [orderId]: false }));
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
            fetchOrders();
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
      setLoading((prev) => ({ ...prev, [orderId]: false }));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to initiate Razorpay payment');
      setLoading((prev) => ({ ...prev, [orderId]: false }));
    }
  };

  const badgeClass = (status) => {
    const map = {
      PENDING_PAYMENT: 'badge-pending',
      PLACED: 'badge-pending',
      CONFIRMED: 'badge-confirmed',
      PAID: 'badge-confirmed',
      ASSIGNED: 'badge-assigned',
      PREPARING: 'badge-assigned',
      OUT_FOR_DELIVERY: 'badge-assigned',
      COMPLETED: 'badge-completed',
      CANCELLED: 'badge-failed',
      DELIVERED: 'badge-delivered',
    };
    return `badge ${map[status] || 'badge-pending'}`;
  };

  return (
    <div className="container">
      <div className="dashboard-header">
        <h2>My Orders</h2>
        <Link to="/customer" className="btn btn-secondary btn-sm">&larr; Back to Dashboard</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {orders.length === 0 ? (
        <p>No orders yet. <Link to="/customer">Browse restaurants</Link></p>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>Order #{order.id}</h3>
              <span className={badgeClass(order.status)}>{order.status}</span>
            </div>
            <p>Restaurant ID: {order.restaurantId}</p>
            <p>Total: <strong>${Number(order.totalAmount).toFixed(2)}</strong></p>
            <p>Address: {order.deliveryAddress}</p>
            <p style={{ fontSize: '0.85rem', color: '#888' }}>
              Placed: {new Date(order.createdAt).toLocaleString()}
            </p>
            {order.items && (
              <div className="order-items">
                {order.items.map((item, idx) => (
                  <div key={idx} className="order-item">
                    <span>{item.menuItemName} x{item.quantity}</span>
                    <span>${(item.price * item.quantity).toFixed(2)}</span>
                  </div>
                ))}
              </div>
            )}
            <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
              {(order.status === 'PENDING_PAYMENT' || order.status === 'PLACED') && (
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => handleRazorpayPayment(order.id)}
                  disabled={loading[order.id]}
                >
                  {loading[order.id] ? 'Processing...' : '💳 Pay Now'}
                </button>
              )}
              <Link to={`/track/${order.id}`} className="btn btn-secondary btn-sm">
                Track Order
              </Link>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

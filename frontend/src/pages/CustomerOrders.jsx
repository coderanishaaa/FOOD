import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../api/axios';
import StripeCheckout from '../components/StripeCheckout';

export default function CustomerOrders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState({});
  const [selectedOrderForPayment, setSelectedOrderForPayment] = useState(null);

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

  const handlePayment = (orderId) => {
    setSelectedOrderForPayment(orderId);
    setError('');
  };

  const handlePaymentComplete = () => {
    setSelectedOrderForPayment(null);
    // Refresh orders to see updated status
    fetchOrders();
    // Optionally navigate to success page
    navigate('/payment/success', { replace: true });
  };

  const handlePaymentCancel = () => {
    setSelectedOrderForPayment(null);
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

  // If showing checkout for a specific order, render the payment component
  if (selectedOrderForPayment) {
    return (
      <div className="container">
        <div className="dashboard-header">
          <h2>Payment for Order #{selectedOrderForPayment}</h2>
          <button 
            className="btn btn-secondary btn-sm" 
            onClick={handlePaymentCancel}
          >
            &larr; Back to Orders
          </button>
        </div>
        <StripeCheckout 
          orderId={selectedOrderForPayment}
          onPaymentComplete={handlePaymentComplete}
          onCancel={handlePaymentCancel}
        />
      </div>
    );
  }

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
                  className="btn btn-success btn-sm"
                  onClick={() => handlePayment(order.id)}
                  disabled={loading[order.id]}
                >
                  {loading[order.id] ? 'Processing...' : '💳 Pay Now'}
                </button>
              )}
              <Link to={`/track/${order.id}`} className="btn btn-primary btn-sm">
                Track Order
              </Link>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

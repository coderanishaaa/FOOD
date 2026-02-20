import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import API from '../api/axios';

export default function CustomerOrders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

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

  const badgeClass = (status) => {
    const map = {
      PLACED: 'badge-pending', CONFIRMED: 'badge-confirmed',
      PREPARING: 'badge-assigned', COMPLETED: 'badge-completed',
      CANCELLED: 'badge-failed', DELIVERED: 'badge-delivered',
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
            <Link to={`/track/${order.id}`} className="btn btn-primary btn-sm" style={{ marginTop: 8 }}>
              Track Order
            </Link>
          </div>
        ))
      )}
    </div>
  );
}

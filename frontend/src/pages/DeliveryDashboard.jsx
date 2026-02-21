import { useState, useEffect } from 'react';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function DeliveryDashboard() {
  const { user } = useAuth();
  const [deliveries, setDeliveries] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const [availableDeliveries, setAvailableDeliveries] = useState([]);

  useEffect(() => {
    fetchDeliveries();
    fetchAvailableDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const res = await API.get('/api/deliveries/my-deliveries');
      setDeliveries(res.data.data || []);
    } catch (err) { setError('Failed to load deliveries'); }
  };

  const fetchAvailableDeliveries = async () => {
    try {
      const res = await API.get('/api/deliveries/available');
      setAvailableDeliveries(res.data.data || []);
    } catch (err) { setError('Failed to load available deliveries'); }
  };

  const assignDelivery = async (deliveryId) => {
    try {
      await API.put(`/api/deliveries/${deliveryId}/assign`);
      setMessage('Delivery accepted!');
      fetchDeliveries();
      fetchAvailableDeliveries();
    } catch (err) { setError('Failed to accept delivery'); }
  };

  const updateStatus = async (deliveryId, newStatus) => {
    try {
      await API.put(`/api/deliveries/${deliveryId}/status?status=${newStatus}`);
      setMessage(`Delivery updated to ${newStatus}`);
      fetchDeliveries();
    } catch (err) { setError('Failed to update status'); }
  };

  const statusActions = {
    ASSIGNED: [{ label: 'Pick Up', status: 'PICKED_UP' }],
    PICKED_UP: [{ label: 'Mark Delivered', status: 'DELIVERED' }],
    DELIVERED: [],
  };

  const badgeClass = (status) => {
    const map = { ASSIGNED: 'badge-assigned', PICKED_UP: 'badge-picked-up', DELIVERED: 'badge-delivered' };
    return `badge ${map[status] || 'badge-pending'}`;
  };

  return (
    <div className="container">
      <div className="dashboard-header">
        <h2>Delivery Dashboard</h2>
        <button className="btn btn-secondary btn-sm" onClick={() => { fetchDeliveries(); fetchAvailableDeliveries(); }}>Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}


      {/* Available Deliveries Section */}
      <h3 style={{ marginTop: 24, marginBottom: 16 }}>Available Deliveries</h3>
      {availableDeliveries.length === 0 ? (
        <p>No new deliveries available right now.</p>
      ) : (
        availableDeliveries.map((d) => (
          <div key={d.id} className="card" style={{ borderLeft: '4px solid #f39c12' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>Delivery #{d.id}</h3>
              <span className="badge badge-pending">PENDING ACCEPTANCE</span>
            </div>
            <p>Order ID: {d.orderId}</p>
            <p>Address: {d.deliveryAddress}</p>
            <p style={{ fontSize: '0.85rem', color: '#888' }}>
              Created: {new Date(d.createdAt).toLocaleString()}
            </p>
            <div style={{ marginTop: 8 }}>
              <button className="btn btn-success btn-sm" onClick={() => assignDelivery(d.id)}>
                Accept Delivery
              </button>
            </div>
          </div>
        ))
      )}

      {/* My Deliveries Section */}
      <h3 style={{ marginTop: 32, marginBottom: 16 }}>My Active Deliveries</h3>
      {deliveries.length === 0 ? (
        <p>No deliveries assigned yet.</p>
      ) : (
        deliveries.map((d) => (
          <div key={d.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>Delivery #{d.id}</h3>
              <span className={badgeClass(d.status)}>{d.status}</span>
            </div>
            <p>Order ID: {d.orderId}</p>
            <p>Address: {d.deliveryAddress}</p>
            <p style={{ fontSize: '0.85rem', color: '#888' }}>
              Created: {new Date(d.createdAt).toLocaleString()}
            </p>
            <div style={{ marginTop: 8, display: 'flex', gap: 8 }}>
              {(statusActions[d.status] || []).map((action) => (
                <button key={action.status} className="btn btn-primary btn-sm"
                  onClick={() => updateStatus(d.id, action.status)}>
                  {action.label}
                </button>
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

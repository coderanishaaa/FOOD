import { useState, useEffect } from 'react';
import API from '../api/axios';

/**
 * Admin Dashboard showing system-wide notifications and
 * the ability to view all restaurants.
 */
export default function AdminDashboard() {
  const [notifications, setNotifications] = useState([]);
  const [restaurants, setRestaurants] = useState([]);
  const [activeTab, setActiveTab] = useState('notifications');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchNotifications();
    fetchRestaurants();
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await API.get('/api/notifications');
      setNotifications(res.data.data || []);
    } catch (err) { setError('Failed to load notifications'); }
  };

  const fetchRestaurants = async () => {
    try {
      const res = await API.get('/api/restaurants');
      setRestaurants(res.data.data || []);
    } catch { /* ignore */ }
  };

  const badgeClass = (type) => {
    const map = { ORDER: 'badge-pending', PAYMENT: 'badge-completed', DELIVERY: 'badge-assigned' };
    return `badge ${map[type] || 'badge-pending'}`;
  };

  return (
    <div className="container">
      <h2>Admin Dashboard</h2>

      <div style={{ display: 'flex', gap: 8, margin: '16px 0' }}>
        <button className={`btn ${activeTab === 'notifications' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('notifications')}>
          Notifications ({notifications.length})
        </button>
        <button className={`btn ${activeTab === 'restaurants' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('restaurants')}>
          Restaurants ({restaurants.length})
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {activeTab === 'notifications' && (
        <>
          {notifications.length === 0 ? (
            <p>No notifications yet.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Order</th>
                  <th>Status</th>
                  <th>Message</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((n) => (
                  <tr key={n.id}>
                    <td>{n.id}</td>
                    <td><span className={badgeClass(n.type)}>{n.type}</span></td>
                    <td>#{n.orderId}</td>
                    <td>{n.status}</td>
                    <td style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}>{n.message}</td>
                    <td style={{ fontSize: '0.85rem' }}>{new Date(n.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {activeTab === 'restaurants' && (
        <div className="grid">
          {restaurants.map((r) => (
            <div key={r.id} className="card">
              <h3>{r.name}</h3>
              <p>{r.cuisine}</p>
              <p style={{ color: '#888' }}>{r.address}</p>
              <p style={{ fontSize: '0.85rem' }}>Owner ID: {r.ownerId}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

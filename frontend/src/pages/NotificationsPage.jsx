import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../context/NotificationContext';

/**
 * Full-page view of all notifications for the authenticated user.
 * Supports marking as read and clearing all.
 */
export default function NotificationsPage() {
  const { notifications, markAsRead, clearAll, unreadCount } = useNotifications();
  const navigate = useNavigate();

  const handleClick = (notification) => {
    if (!notification.readStatus) {
      markAsRead(notification.id);
    }
    if (notification.orderId) {
      navigate(`/track/${notification.orderId}`);
    }
  };

  const typeBadgeClass = (type) => {
    switch (type) {
      case 'ORDER': return 'badge badge-pending';
      case 'PAYMENT': return 'badge badge-completed';
      case 'DELIVERY': return 'badge badge-assigned';
      default: return 'badge badge-confirmed';
    }
  };

  return (
    <div className="container">
      <div className="dashboard-header">
        <h2>Notifications {unreadCount > 0 && <span className="unread-count">({unreadCount} unread)</span>}</h2>
        {notifications.length > 0 && (
          <button className="btn btn-danger btn-sm" onClick={clearAll}>Clear All</button>
        )}
      </div>

      {notifications.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <p style={{ color: '#888', fontSize: '1.1rem' }}>No notifications yet</p>
        </div>
      ) : (
        <div className="notification-list">
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`card notification-card ${!n.readStatus ? 'notification-unread' : ''}`}
              onClick={() => handleClick(n)}
              style={{ cursor: 'pointer' }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className={typeBadgeClass(n.type)}>{n.type}</span>
                <span style={{ fontSize: '0.8rem', color: '#888' }}>
                  {new Date(n.createdAt).toLocaleString()}
                </span>
              </div>
              <p style={{ marginTop: 8 }}>{n.message}</p>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
                <span style={{ fontSize: '0.85rem', color: '#555' }}>Order #{n.orderId}</span>
                {!n.readStatus && (
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={(e) => { e.stopPropagation(); markAsRead(n.id); }}
                  >
                    Mark as Read
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

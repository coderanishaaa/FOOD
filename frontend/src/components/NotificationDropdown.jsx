import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../context/NotificationContext';

/**
 * Dropdown list showing recent notifications.
 * Appears below the NotificationBell.
 */
export default function NotificationDropdown({ onClose }) {
  const { notifications, markAsRead, clearAll } = useNotifications();
  const navigate = useNavigate();

  const recent = notifications.slice(0, 10);

  const handleClick = (notification) => {
    if (!notification.readStatus) {
      markAsRead(notification.id);
    }
    // Navigate to order tracking if it's an order-related notification
    if (notification.orderId) {
      navigate(`/track/${notification.orderId}`);
    }
    onClose();
  };

  const typeIcon = (type) => {
    switch (type) {
      case 'ORDER': return 'order-icon';
      case 'PAYMENT': return 'payment-icon';
      case 'DELIVERY': return 'delivery-icon';
      default: return 'system-icon';
    }
  };

  return (
    <div className="notification-dropdown">
      <div className="notification-dropdown-header">
        <span className="notification-dropdown-title">Notifications</span>
        <div className="notification-dropdown-actions">
          {notifications.length > 0 && (
            <button className="notification-link-btn" onClick={() => { clearAll(); }}>
              Clear All
            </button>
          )}
          <button
            className="notification-link-btn"
            onClick={() => { navigate('/notifications'); onClose(); }}
          >
            View All
          </button>
        </div>
      </div>

      <div className="notification-dropdown-list">
        {recent.length === 0 ? (
          <div className="notification-empty">No notifications</div>
        ) : (
          recent.map((n) => (
            <div
              key={n.id}
              className={`notification-dropdown-item ${!n.readStatus ? 'unread' : ''}`}
              onClick={() => handleClick(n)}
            >
              <div className={`notification-type-dot ${typeIcon(n.type)}`} />
              <div className="notification-item-content">
                <p className="notification-item-message">{n.message}</p>
                <span className="notification-item-time">
                  {new Date(n.createdAt).toLocaleString()}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

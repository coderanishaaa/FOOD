import { useNotifications } from '../context/NotificationContext';

/**
 * Renders toast popup notifications in the bottom-right corner.
 * Each toast auto-dismisses after 5 seconds or can be dismissed manually.
 */
export default function NotificationToast() {
  const { toasts, dismissToast } = useNotifications();

  if (toasts.length === 0) return null;

  const typeLabel = (type) => {
    switch (type) {
      case 'ORDER': return 'Order Update';
      case 'PAYMENT': return 'Payment Update';
      case 'DELIVERY': return 'Delivery Update';
      default: return 'Notification';
    }
  };

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <div key={toast.toastId} className={`toast toast-${(toast.type || 'system').toLowerCase()}`}>
          <div className="toast-header">
            <strong>{typeLabel(toast.type)}</strong>
            <button
              className="toast-close"
              onClick={() => dismissToast(toast.toastId)}
              aria-label="Dismiss notification"
            >
              &times;
            </button>
          </div>
          <p className="toast-message">{toast.message}</p>
        </div>
      ))}
    </div>
  );
}

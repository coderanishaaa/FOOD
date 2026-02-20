import API from '../api/axios';

/**
 * Notification REST API service.
 * All calls go through the API Gateway → notification-service.
 */
const notificationService = {
  /** Get all notifications for a user */
  getByUserId: (userId) =>
    API.get(`/api/notifications/user/${userId}`).then((r) => r.data.data),

  /** Get unread notifications for a user */
  getUnread: (userId) =>
    API.get(`/api/notifications/user/${userId}/unread`).then((r) => r.data.data),

  /** Mark a single notification as read */
  markAsRead: (notificationId) =>
    API.put(`/api/notifications/${notificationId}/read`).then((r) => r.data.data),

  /** Clear all notifications for a user */
  clearAll: (userId) =>
    API.delete(`/api/notifications/user/${userId}/clear`).then((r) => r.data),
};

export default notificationService;

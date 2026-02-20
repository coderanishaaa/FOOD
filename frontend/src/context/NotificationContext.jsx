import { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs';
import { useAuth } from './AuthContext';
import notificationService from '../services/notificationService';

const NotificationContext = createContext(null);

/**
 * Provides real-time notification state and WebSocket management.
 * - Connects after login, disconnects on logout.
 * - Subscribes to /topic/notifications/{userId} for real-time push.
 * - Exposes notification CRUD operations and toast queue.
 */
export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [toasts, setToasts] = useState([]);
  const stompClient = useRef(null);

  // Fetch notifications from REST API
  const fetchNotifications = useCallback(async () => {
    if (!user?.id) return;
    try {
      const all = await notificationService.getByUserId(user.id);
      setNotifications(all || []);
      const unread = (all || []).filter((n) => !n.readStatus);
      setUnreadCount(unread.length);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    }
  }, [user?.id]);

  // Add a toast notification (auto-dismiss after 5s)
  const addToast = useCallback((notification) => {
    const toastId = Date.now() + Math.random();
    const toast = { ...notification, toastId };
    setToasts((prev) => [...prev, toast]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.toastId !== toastId));
    }, 5000);
  }, []);

  // Dismiss a specific toast
  const dismissToast = useCallback((toastId) => {
    setToasts((prev) => prev.filter((t) => t.toastId !== toastId));
  }, []);

  // Connect to WebSocket
  useEffect(() => {
    if (!user?.id) {
      // Disconnect if logged out
      if (stompClient.current) {
        stompClient.current.disconnect();
        stompClient.current = null;
      }
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    // Fetch existing notifications on login
    fetchNotifications();

    // Build WebSocket URL — use gateway in Docker, direct in dev
    const wsBaseUrl = import.meta.env.VITE_WS_URL || import.meta.env.VITE_API_URL || '';
    const wsUrl = `${wsBaseUrl}/ws`;

    const socket = new SockJS(wsUrl);
    const client = Stomp.over(socket);
    // Disable debug logging in production
    client.debug = null;

    client.connect({}, () => {
      const destination = `/topic/notifications/${user.id}`;
      client.subscribe(destination, (message) => {
        try {
          const notification = JSON.parse(message.body);
          setNotifications((prev) => [notification, ...prev]);
          setUnreadCount((prev) => prev + 1);
          addToast(notification);
        } catch (e) {
          console.error('Failed to parse WebSocket notification:', e);
        }
      });
    }, (error) => {
      console.error('WebSocket connection error:', error);
    });

    stompClient.current = client;

    return () => {
      if (stompClient.current) {
        stompClient.current.disconnect();
        stompClient.current = null;
      }
    };
  }, [user?.id, fetchNotifications, addToast]);

  // Mark notification as read
  const markAsRead = useCallback(async (notificationId) => {
    try {
      await notificationService.markAsRead(notificationId);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, readStatus: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  }, []);

  // Clear all notifications
  const clearAll = useCallback(async () => {
    if (!user?.id) return;
    try {
      await notificationService.clearAll(user.id);
      setNotifications([]);
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to clear notifications:', err);
    }
  }, [user?.id]);

  const value = {
    notifications,
    unreadCount,
    toasts,
    fetchNotifications,
    markAsRead,
    clearAll,
    dismissToast,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}

export const useNotifications = () => {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error('useNotifications must be used within NotificationProvider');
  return ctx;
};

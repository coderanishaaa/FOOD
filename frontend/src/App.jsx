import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import CustomerDashboard from './pages/CustomerDashboard';
import CustomerOrders from './pages/CustomerOrders';
import OrderTracking from './pages/OrderTracking';
import PaymentSuccess from './pages/PaymentSuccess';
import RestaurantDashboard from './pages/RestaurantDashboard';
import DeliveryDashboard from './pages/DeliveryDashboard';
import AdminDashboard from './pages/AdminDashboard';
import NotificationsPage from './pages/NotificationsPage';

/**
 * Root component — defines all application routes with role-based guards.
 */
export default function App() {
  const { user, loading } = useAuth();

  if (loading) return <div className="loading">Loading...</div>;

  /** Redirect authenticated users from / to their dashboard */
  const dashboardPath = {
    CUSTOMER: '/customer',
    RESTAURANT_OWNER: '/restaurant',
    DELIVERY_AGENT: '/delivery',
    ADMIN: '/admin',
  };

  return (
    <>
      <Navbar />
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={user ? <Navigate to={dashboardPath[user.role] || '/'} /> : <Login />} />
        <Route path="/register" element={user ? <Navigate to={dashboardPath[user.role] || '/'} /> : <Register />} />

        {/* Customer routes */}
        <Route path="/customer" element={
          <ProtectedRoute roles={['CUSTOMER']}>
            <CustomerDashboard />
          </ProtectedRoute>
        } />
        <Route path="/customer/orders" element={
          <ProtectedRoute roles={['CUSTOMER']}>
            <CustomerOrders />
          </ProtectedRoute>
        } />
        <Route path="/track/:orderId" element={
          <ProtectedRoute roles={['CUSTOMER', 'ADMIN']}>
            <OrderTracking />
          </ProtectedRoute>
        } />
        <Route path="/payment/success" element={
          <ProtectedRoute roles={['CUSTOMER']}>
            <PaymentSuccess />
          </ProtectedRoute>
        } />

        {/* Restaurant Owner routes */}
        <Route path="/restaurant" element={
          <ProtectedRoute roles={['RESTAURANT_OWNER']}>
            <RestaurantDashboard />
          </ProtectedRoute>
        } />

        {/* Delivery Agent routes */}
        <Route path="/delivery" element={
          <ProtectedRoute roles={['DELIVERY_AGENT']}>
            <DeliveryDashboard />
          </ProtectedRoute>
        } />

        {/* Notifications route (all authenticated users) */}
        <Route path="/notifications" element={
          <ProtectedRoute roles={['CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_AGENT', 'ADMIN']}>
            <NotificationsPage />
          </ProtectedRoute>
        } />

        {/* Admin routes */}
        <Route path="/admin" element={
          <ProtectedRoute roles={['ADMIN']}>
            <AdminDashboard />
          </ProtectedRoute>
        } />

        {/* Default redirect */}
        <Route path="/" element={
          user ? <Navigate to={dashboardPath[user.role] || '/login'} /> : <Navigate to="/login" />
        } />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </>
  );
}

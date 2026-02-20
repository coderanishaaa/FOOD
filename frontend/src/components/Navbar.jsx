import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  /** Map each role to its dashboard path */
  const dashboardPath = {
    CUSTOMER: '/customer',
    RESTAURANT_OWNER: '/restaurant',
    DELIVERY_AGENT: '/delivery',
    ADMIN: '/admin',
  };

  return (
    <nav className="navbar">
      <Link to="/" className="brand">FoodDelivery</Link>
      <div className="nav-links">
        {user ? (
          <>
            <Link to={dashboardPath[user.role] || '/'}>Dashboard</Link>
            {user.role === 'CUSTOMER' && <Link to="/customer/orders">My Orders</Link>}
            <span style={{ color: '#bbb' }}>Hi, {user.name}</span>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

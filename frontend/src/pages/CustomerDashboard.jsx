import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [cart, setCart] = useState([]);
  const [address, setAddress] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchRestaurants();
  }, []);

  const fetchRestaurants = async () => {
    try {
      const res = await API.get('/api/restaurants');
      setRestaurants(res.data.data || []);
    } catch (err) {
      setError('Failed to load restaurants');
    }
  };

  const selectRestaurant = async (restaurant) => {
    setSelectedRestaurant(restaurant);
    setCart([]);
    setMessage('');
    try {
      const res = await API.get(`/api/menu-items/restaurant/${restaurant.id}`);
      setMenuItems(res.data.data || []);
    } catch (err) {
      setError('Failed to load menu');
    }
  };

  const addToCart = (item) => {
    const existing = cart.find((c) => c.menuItemId === item.id);
    if (existing) {
      setCart(cart.map((c) =>
        c.menuItemId === item.id ? { ...c, quantity: c.quantity + 1 } : c
      ));
    } else {
      setCart([...cart, {
        menuItemId: item.id,
        menuItemName: item.name,
        quantity: 1,
        price: item.price,
      }]);
    }
  };

  const removeFromCart = (menuItemId) => {
    setCart(cart.filter((c) => c.menuItemId !== menuItemId));
  };

  const placeOrder = async () => {
    if (cart.length === 0) return setError('Cart is empty');
    if (!address.trim()) return setError('Please enter a delivery address');
    setError('');
    try {
      await API.post('/api/orders', {
        restaurantId: selectedRestaurant.id,
        items: cart,
        deliveryAddress: address,
      });
      setMessage('Order placed successfully!');
      setCart([]);
      setAddress('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order');
    }
  };

  const cartTotal = cart.reduce((sum, c) => sum + c.price * c.quantity, 0);

  return (
    <div className="container">
      <div className="dashboard-header">
        <h2>Welcome, {user?.name}!</h2>
        <button className="btn btn-secondary" onClick={() => navigate('/customer/orders')}>
          My Orders
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}

      {!selectedRestaurant ? (
        <>
          <h3>Browse Restaurants</h3>
          <div className="grid">
            {restaurants.map((r) => (
              <div key={r.id} className="card" style={{ cursor: 'pointer' }} onClick={() => selectRestaurant(r)}>
                <h3>{r.name}</h3>
                <p>{r.cuisine}</p>
                <p style={{ color: '#888', fontSize: '0.9rem' }}>{r.address}</p>
              </div>
            ))}
            {restaurants.length === 0 && <p>No restaurants available</p>}
          </div>
        </>
      ) : (
        <>
          <button className="btn btn-secondary btn-sm" onClick={() => { setSelectedRestaurant(null); setCart([]); }}>
            &larr; Back to Restaurants
          </button>
          <h3 style={{ marginTop: 16 }}>{selectedRestaurant.name} — Menu</h3>

          <div style={{ display: 'flex', gap: 24, marginTop: 16 }}>
            {/* Menu Items */}
            <div style={{ flex: 2 }}>
              <div className="grid">
                {menuItems.map((item) => (
                  <div key={item.id} className="card">
                    <h3>{item.name}</h3>
                    <p style={{ color: '#888' }}>{item.description}</p>
                    <p style={{ fontWeight: 'bold', color: '#27ae60' }}>${Number(item.price).toFixed(2)}</p>
                    <button className="btn btn-primary btn-sm" onClick={() => addToCart(item)}>Add to Cart</button>
                  </div>
                ))}
                {menuItems.length === 0 && <p>No menu items available</p>}
              </div>
            </div>

            {/* Cart */}
            <div style={{ flex: 1 }}>
              <div className="card">
                <h3>Cart ({cart.length})</h3>
                {cart.map((c) => (
                  <div key={c.menuItemId} className="order-item">
                    <span>{c.menuItemName} x{c.quantity}</span>
                    <span>
                      ${(c.price * c.quantity).toFixed(2)}
                      <button className="btn btn-danger btn-sm" style={{ marginLeft: 8 }}
                        onClick={() => removeFromCart(c.menuItemId)}>✕</button>
                    </span>
                  </div>
                ))}
                {cart.length > 0 && (
                  <>
                    <hr style={{ margin: '12px 0' }} />
                    <p style={{ fontWeight: 'bold' }}>Total: ${cartTotal.toFixed(2)}</p>
                    <div className="form-group" style={{ marginTop: 12 }}>
                      <label>Delivery Address</label>
                      <input value={address} onChange={(e) => setAddress(e.target.value)}
                        placeholder="Enter delivery address" />
                    </div>
                    <button className="btn btn-success" style={{ width: '100%' }} onClick={placeOrder}>
                      Place Order
                    </button>
                  </>
                )}
                {cart.length === 0 && <p style={{ color: '#888' }}>Your cart is empty</p>}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

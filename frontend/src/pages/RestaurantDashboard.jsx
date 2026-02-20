import { useState, useEffect } from 'react';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function RestaurantDashboard() {
  const { user } = useAuth();
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  // Restaurant form
  const [restForm, setRestForm] = useState({ name: '', address: '', phone: '', cuisine: '', imageUrl: '' });
  const [showRestForm, setShowRestForm] = useState(false);

  // Menu item form
  const [menuForm, setMenuForm] = useState({ name: '', description: '', price: '', category: '', imageUrl: '' });
  const [showMenuForm, setShowMenuForm] = useState(false);

  useEffect(() => { fetchRestaurants(); }, []);

  const fetchRestaurants = async () => {
    try {
      const res = await API.get('/api/restaurants/owner');
      setRestaurants(res.data.data || []);
    } catch (err) { setError('Failed to load restaurants'); }
  };

  const createRestaurant = async (e) => {
    e.preventDefault();
    try {
      await API.post('/api/restaurants', restForm);
      setMessage('Restaurant created!');
      setShowRestForm(false);
      setRestForm({ name: '', address: '', phone: '', cuisine: '', imageUrl: '' });
      fetchRestaurants();
    } catch (err) { setError(err.response?.data?.message || 'Failed to create restaurant'); }
  };

  const selectRestaurant = async (rest) => {
    setSelectedRestaurant(rest);
    setMessage('');
    try {
      const [menuRes, orderRes] = await Promise.all([
        API.get(`/api/menu-items/restaurant/${rest.id}`),
        API.get(`/api/orders/restaurant/${rest.id}`),
      ]);
      setMenuItems(menuRes.data.data || []);
      setOrders(orderRes.data.data || []);
    } catch (err) { setError('Failed to load data'); }
  };

  const addMenuItem = async (e) => {
    e.preventDefault();
    try {
      await API.post('/api/menu-items', { ...menuForm, restaurantId: selectedRestaurant.id, price: parseFloat(menuForm.price) });
      setMessage('Menu item added!');
      setShowMenuForm(false);
      setMenuForm({ name: '', description: '', price: '', category: '', imageUrl: '' });
      selectRestaurant(selectedRestaurant);
    } catch (err) { setError(err.response?.data?.message || 'Failed to add menu item'); }
  };

  const deleteMenuItem = async (id) => {
    try {
      await API.delete(`/api/menu-items/${id}`);
      setMenuItems(menuItems.filter((m) => m.id !== id));
    } catch (err) { setError('Failed to delete item'); }
  };

  return (
    <div className="container">
      <div className="dashboard-header">
        <h2>Restaurant Owner Dashboard</h2>
        {!selectedRestaurant && (
          <button className="btn btn-primary" onClick={() => setShowRestForm(!showRestForm)}>
            {showRestForm ? 'Cancel' : '+ New Restaurant'}
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}

      {/* Create Restaurant Form */}
      {showRestForm && (
        <div className="card">
          <h3>New Restaurant</h3>
          <form onSubmit={createRestaurant}>
            {['name', 'address', 'phone', 'cuisine'].map((f) => (
              <div className="form-group" key={f}>
                <label>{f.charAt(0).toUpperCase() + f.slice(1)}</label>
                <input value={restForm[f]} onChange={(e) => setRestForm({ ...restForm, [f]: e.target.value })} required={f !== 'phone'} />
              </div>
            ))}
            <button type="submit" className="btn btn-primary">Create</button>
          </form>
        </div>
      )}

      {!selectedRestaurant ? (
        /* Restaurant List */
        <div className="grid">
          {restaurants.map((r) => (
            <div key={r.id} className="card" style={{ cursor: 'pointer' }} onClick={() => selectRestaurant(r)}>
              <h3>{r.name}</h3>
              <p>{r.cuisine} — {r.address}</p>
            </div>
          ))}
          {restaurants.length === 0 && !showRestForm && <p>No restaurants yet. Create one!</p>}
        </div>
      ) : (
        <>
          <button className="btn btn-secondary btn-sm" onClick={() => setSelectedRestaurant(null)}>
            &larr; Back to Restaurants
          </button>
          <h3 style={{ marginTop: 16 }}>{selectedRestaurant.name}</h3>

          {/* Menu Items Section */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16 }}>
            <h3>Menu Items</h3>
            <button className="btn btn-primary btn-sm" onClick={() => setShowMenuForm(!showMenuForm)}>
              {showMenuForm ? 'Cancel' : '+ Add Item'}
            </button>
          </div>

          {showMenuForm && (
            <div className="card">
              <form onSubmit={addMenuItem}>
                {['name', 'description', 'price', 'category'].map((f) => (
                  <div className="form-group" key={f}>
                    <label>{f.charAt(0).toUpperCase() + f.slice(1)}</label>
                    <input
                      type={f === 'price' ? 'number' : 'text'}
                      step={f === 'price' ? '0.01' : undefined}
                      value={menuForm[f]}
                      onChange={(e) => setMenuForm({ ...menuForm, [f]: e.target.value })}
                      required={f === 'name' || f === 'price'}
                    />
                  </div>
                ))}
                <button type="submit" className="btn btn-primary">Add Item</button>
              </form>
            </div>
          )}

          <div className="grid">
            {menuItems.map((item) => (
              <div key={item.id} className="card">
                <h3>{item.name}</h3>
                <p style={{ color: '#888' }}>{item.description}</p>
                <p><strong>${Number(item.price).toFixed(2)}</strong> — {item.category}</p>
                <button className="btn btn-danger btn-sm" onClick={() => deleteMenuItem(item.id)}>Delete</button>
              </div>
            ))}
          </div>

          {/* Orders Section */}
          <h3 style={{ marginTop: 24 }}>Orders ({orders.length})</h3>
          {orders.map((order) => (
            <div key={order.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Order #{order.id}</span>
                <span className={`badge badge-${order.status.toLowerCase()}`}>{order.status}</span>
              </div>
              <p>Total: ${Number(order.totalAmount).toFixed(2)}</p>
              <p>Address: {order.deliveryAddress}</p>
            </div>
          ))}
        </>
      )}
    </div>
  );
}

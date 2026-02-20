import axios from 'axios';

/**
 * Axios instance pre-configured to hit the API Gateway.
 * In dev mode, Vite proxies /api → localhost:8080.
 * In Docker, VITE_API_URL env var points to the gateway.
 */
const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token and User ID to every request if present
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const user = localStorage.getItem('user');
  if (user) {
    try {
      const userData = JSON.parse(user);
      if (userData.id) {
        config.headers['X-User-Id'] = userData.id;
      }
    } catch (e) {
      // Ignore parsing errors
    }
  }
  return config;
});

// Handle 401 responses globally — force logout
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default API;

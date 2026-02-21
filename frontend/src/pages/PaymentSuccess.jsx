import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import API from '../api/axios';

/**
 * Payment Success Page
 * Handles redirects from Stripe Checkout after successful payment.
 * Also handles mock payment completion.
 */
export default function PaymentSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [orderId, setOrderId] = useState(null);

  useEffect(() => {
    const sessionId = searchParams.get('session_id');
    const orderIdParam = searchParams.get('order_id');
    const mock = searchParams.get('mock');

    if (orderIdParam) {
      setOrderId(parseInt(orderIdParam));
    }

    // Handle mock payment completion
    if (mock === 'true' && orderIdParam) {
      handleMockPaymentCompletion(orderIdParam);
      return;
    }

    // Handle real Stripe payment
    if (sessionId && orderIdParam) {
      // Payment was successful - Stripe webhook will handle the rest
      // Just wait a moment and redirect
      setTimeout(() => {
        setLoading(false);
        navigate(`/customer/orders?payment_success=true&order_id=${orderIdParam}`, { replace: true });
      }, 2000);
    } else {
      // No session ID - redirect to orders
      setLoading(false);
      navigate('/customer/orders', { replace: true });
    }
  }, [searchParams, navigate]);

  const handleMockPaymentCompletion = async (orderId) => {
    try {
      setLoading(true);
      // Call mock payment completion endpoint
      await API.post(`/api/payments/mock-complete/${orderId}`);
      
      // Wait a moment for backend to process
      setTimeout(() => {
        setLoading(false);
        navigate(`/customer/orders?payment_success=true&order_id=${orderId}`, { replace: true });
      }, 1500);
    } catch (err) {
      console.error('Error completing mock payment:', err);
      setError(err.response?.data?.message || 'Failed to complete payment');
      setLoading(false);
      
      // Still redirect after showing error
      setTimeout(() => {
        navigate(`/customer/orders?order_id=${orderId}`, { replace: true });
      }, 3000);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '50px' }}>
        <div className="spinner"></div>
        <h2>Processing your payment...</h2>
        <p>Please wait while we confirm your payment.</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '50px' }}>
        <div className="alert alert-error">{error}</div>
        <button 
          className="btn btn-primary" 
          onClick={() => navigate('/customer/orders')}
        >
          Go to Orders
        </button>
      </div>
    );
  }

  return (
    <div className="container" style={{ textAlign: 'center', padding: '50px' }}>
      <div className="alert alert-success">
        <h2>✅ Payment Successful!</h2>
        <p>Your payment has been processed successfully.</p>
        {orderId && <p>Order ID: #{orderId}</p>}
      </div>
      <button 
        className="btn btn-primary" 
        onClick={() => navigate('/customer/orders')}
      >
        View My Orders
      </button>
    </div>
  );
}

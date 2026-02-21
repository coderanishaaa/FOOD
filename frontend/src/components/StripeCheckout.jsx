import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../api/axios';

/**
 * Stripe Checkout Component - Redirect Flow
 * Creates a Stripe Checkout Session and redirects to Stripe's hosted checkout page.
 * This is simpler and more secure than embedded checkout.
 */
export default function StripeCheckout({ orderId, onPaymentComplete, onCancel }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // Immediately create checkout session and redirect
    createCheckoutSession();
  }, [orderId]);

  const createCheckoutSession = async () => {
    if (!orderId) {
      setError('Order ID is required');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError('');

      // Call the backend to create Stripe Checkout Session
      const res = await API.post(`/api/payments/create-session/${orderId}`);
      
      if (res.data && res.data.data) {
        const checkoutUrl = res.data.data.url;
        const sessionId = res.data.data.sessionId;

        if (checkoutUrl) {
          // Redirect to Stripe Checkout
          console.log('Redirecting to Stripe Checkout:', checkoutUrl);
          window.location.href = checkoutUrl;
        } else {
          setError('Failed to get checkout URL. Please try again.');
          setLoading(false);
        }
      } else {
        setError('Invalid response from server. Please try again.');
        setLoading(false);
      }
    } catch (err) {
      console.error('Error creating checkout session:', err);
      const errorMsg = err.response?.data?.message || 
                      err.response?.data?.error || 
                      err.message || 
                      'Failed to create payment session. Please try again.';
      setError(errorMsg);
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '40px', minHeight: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
        <div className="spinner" style={{
          border: '4px solid #f3f3f3',
          borderTop: '4px solid #3498db',
          borderRadius: '50%',
          width: '50px',
          height: '50px',
          animation: 'spin 1s linear infinite',
          marginBottom: '20px',
        }} />
        <h3>Preparing your payment...</h3>
        <p>You will be redirected to the payment page shortly.</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ maxWidth: '500px', margin: '0 auto', padding: '20px' }}>
        <div className="alert alert-error" style={{ marginBottom: '20px' }}>
          <h3>Payment Error</h3>
          <p>{error}</p>
        </div>
        
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'space-between' }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onCancel || (() => navigate('/customer/orders'))}
          >
            ← Back to Orders
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={createCheckoutSession}
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </>
  ); // Should not reach here as we redirect immediately
}

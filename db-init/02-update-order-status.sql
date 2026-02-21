-- Update existing orders with PLACED status to PENDING_PAYMENT
-- This ensures backward compatibility with existing orders

USE order_db;

UPDATE orders
SET status = 'PENDING_PAYMENT'
WHERE status = 'PLACED';

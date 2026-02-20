-- Create all databases used by the microservices.
-- Each service uses its own database (database-per-service pattern).
-- Tables are auto-created by Hibernate ddl-auto=update.

CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS restaurant_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS delivery_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Grant privileges (root already has all, but explicit for clarity)
GRANT ALL PRIVILEGES ON user_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
FLUSH PRIVILEGES;

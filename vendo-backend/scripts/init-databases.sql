-- Database Initialization Script for Nexus E-commerce Platform
-- This script creates all required databases and users for Spring Boot services

-- Create main database
CREATE DATABASE nexus_db;

-- Create service-specific databases (if using separate databases)
-- Uncomment if you want separate databases per service
-- CREATE DATABASE nexus_user_db;
-- CREATE DATABASE nexus_product_db;
-- CREATE DATABASE nexus_order_db;
-- CREATE DATABASE nexus_payment_db;
-- CREATE DATABASE nexus_cart_db;
-- CREATE DATABASE nexus_content_db;
-- CREATE DATABASE nexus_notification_db;
-- CREATE DATABASE nexus_analytics_db;
-- CREATE DATABASE nexus_admin_db;
-- CREATE DATABASE nexus_marketplace_db;

-- Create application user with privileges
CREATE USER nexus_app WITH PASSWORD 'nexus_app_password';

-- Grant privileges on main database
GRANT ALL PRIVILEGES ON DATABASE nexus_db TO nexus_app;

-- If using separate databases, grant privileges on each
-- GRANT ALL PRIVILEGES ON DATABASE nexus_user_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_product_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_order_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_payment_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_cart_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_content_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_notification_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_analytics_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_admin_db TO nexus_app;
-- GRANT ALL PRIVILEGES ON DATABASE nexus_marketplace_db TO nexus_app;

-- Connect to main database and grant schema privileges
\c nexus_db;
GRANT ALL PRIVILEGES ON SCHEMA public TO nexus_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO nexus_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO nexus_app;

-- Create read-only user for reporting/analytics
CREATE USER nexus_readonly WITH PASSWORD 'nexus_readonly_password';
GRANT CONNECT ON DATABASE nexus_db TO nexus_readonly;
\c nexus_db;
GRANT USAGE ON SCHEMA public TO nexus_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO nexus_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO nexus_readonly;

-- Display created databases
\l

-- Display created users
\du

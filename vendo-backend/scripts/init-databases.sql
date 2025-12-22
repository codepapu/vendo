-- Database Initialization Script for Vendo E-commerce Platform
-- This script creates all required databases and users for Spring Boot services

-- Create main database
CREATE DATABASE vendo_db;

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
CREATE USER vendo_app WITH PASSWORD 'vendo_app_password';

-- Grant privileges on main database
GRANT ALL PRIVILEGES ON DATABASE vendo_db TO vendo_app;

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
\c vendo_db;
GRANT ALL PRIVILEGES ON SCHEMA public TO vendo_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO vendo_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO vendo_app;

-- Create read-only user for reporting/analytics
CREATE USER vendo_readonly WITH PASSWORD 'vendo_readonly_password';
GRANT CONNECT ON DATABASE vendo_db TO vendo_readonly;
\c vendo_db;
GRANT USAGE ON SCHEMA public TO vendo_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO vendo_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO vendo_readonly;

-- Display created databases
\l

-- Display created users
\du

-- Database Initialization Script for Vendo E-commerce Platform
-- This script creates all required databases and users for Spring Boot services
-- Run this script as PostgreSQL superuser (postgres)

-- ============================================
-- 1. Create Main Database
-- ============================================
-- Drop database if exists (for clean setup)
DROP DATABASE IF EXISTS vendo_db;

-- Create main database
-- Note: OWNER will be set to the user running this script (usually current user on macOS, postgres on Linux)
CREATE DATABASE vendo_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- ============================================
-- 2. Create Application User
-- ============================================
-- Drop user if exists (for clean setup)
DROP USER IF EXISTS vendo_app;

-- Create application user with secure password
-- Password: REDACTED_DB_PASSWORD
CREATE USER vendo_app WITH 
    PASSWORD 'REDACTED_DB_PASSWORD'
    CREATEDB
    NOSUPERUSER
    NOCREATEROLE
    INHERIT
    LOGIN
    REPLICATION
    BYPASSRLS;

-- ============================================
-- 3. Grant Privileges on Database
-- ============================================
-- Grant connection and creation privileges
GRANT CONNECT ON DATABASE vendo_db TO vendo_app;
GRANT CREATE ON DATABASE vendo_db TO vendo_app;

-- ============================================
-- 4. Grant Schema Privileges (run on vendo_db)
-- ============================================
-- Note: These commands need to be run while connected to vendo_db
-- They are handled in the init-databases.sh script

-- ============================================
-- 5. Create Read-Only User for Reporting
-- ============================================
-- Drop user if exists (for clean setup)
DROP USER IF EXISTS vendo_readonly;

-- Create read-only user
-- Password: REDACTED_DB_PASSWORD
CREATE USER vendo_readonly WITH 
    PASSWORD 'REDACTED_DB_PASSWORD'
    NOCREATEDB
    NOSUPERUSER
    NOCREATEROLE
    INHERIT
    LOGIN
    NOREPLICATION
    NOBYPASSRLS;

-- Grant connection privilege
GRANT CONNECT ON DATABASE vendo_db TO vendo_readonly;

-- ============================================
-- Summary
-- ============================================
-- Database: vendo_db
-- Application User: vendo_app
-- Application Password: REDACTED_DB_PASSWORD
-- Read-Only User: vendo_readonly
-- Read-Only Password: REDACTED_DB_PASSWORD
--
-- Next Steps:
-- 1. Run init-databases-schema.sql to grant schema privileges
-- 2. Start Spring Boot application to run Flyway migrations
-- 3. All tables will be created automatically via Flyway

-- ============================================================
-- schema.sql — SQL QueryBuilder Project Database Setup
-- Run once against your MySQL instance:
--   mysql -u root -p < schema.sql
-- ============================================================

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS querybuilder_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE querybuilder_db;

-- ============================================================
-- 2. Drop tables in reverse dependency order (for re-runs)
-- ============================================================
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- ============================================================
-- 3. users table
-- ============================================================
CREATE TABLE users (
    id         INT          PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    age        INT          CHECK (age >= 0 AND age <= 120),
    city       VARCHAR(100),
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. products table
-- ============================================================
CREATE TABLE products (
    id         INT            PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(200)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category   VARCHAR(100),
    stock      INT            DEFAULT 0,
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 5. orders table — references users and products
-- ============================================================
CREATE TABLE orders (
    id          INT            PRIMARY KEY AUTO_INCREMENT,
    user_id     INT            NOT NULL,
    product_id  INT            NOT NULL,
    quantity    INT            DEFAULT 1,
    total_price DECIMAL(10, 2),
    order_date  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================
-- 6. Indexes for common query patterns
-- ============================================================
CREATE INDEX idx_users_city     ON users(city);
CREATE INDEX idx_users_age      ON users(age);
CREATE INDEX idx_products_cat   ON products(category);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_orders_product ON orders(product_id);
CREATE INDEX idx_orders_date    ON orders(order_date);

-- ============================================================
-- 7. Optional seed data (uncomment to pre-populate)
-- ============================================================
-- INSERT INTO users (name, email, age, city) VALUES
--     ('Alice Johnson', 'alice@example.com', 28, 'Mumbai'),
--     ('Bob Smith',     'bob@example.com',   35, 'Delhi'),
--     ('Carol Lee',     'carol@example.com', 24, 'Mumbai'),
--     ('Dave Kumar',    'dave@example.com',  42, 'Pune');
--
-- INSERT INTO products (name, price, category, stock) VALUES
--     ('Laptop Pro',      79999.00, 'Electronics', 50),
--     ('Smartphone X',    24999.00, 'Electronics', 100),
--     ('Standing Desk',   14999.00, 'Furniture',   20),
--     ('Ergonomic Chair',  8999.00, 'Furniture',   30),
--     ('Clean Code',        799.00, 'Books',       200);

SELECT 'Schema created successfully.' AS status;

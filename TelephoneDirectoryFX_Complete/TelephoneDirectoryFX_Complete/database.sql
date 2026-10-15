-- ============================================================
-- TELEPHONE DIRECTORY DATABASE
-- JavaFX + MySQL Project
-- ============================================================

CREATE DATABASE IF NOT EXISTS telephone_directory;
USE telephone_directory;

-- Contact table
CREATE TABLE IF NOT EXISTS contacts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255)
);

-- Login users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

-- Default login:
-- Username: admin
-- Password: admin123
INSERT INTO users (username, password_hash, active)
SELECT 'admin', SHA2('admin123', 256), 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

UPDATE users
SET password_hash = SHA2('admin123', 256), active = 1
WHERE username = 'admin';

-- Sample contacts. They are inserted only when the phone number is absent.
INSERT INTO contacts (name, phone, email, address)
SELECT 'Rahul Patil', '9876543210', 'rahul@gmail.com', 'Chhatrapati Sambhajinagar'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE phone = '9876543210');

INSERT INTO contacts (name, phone, email, address)
SELECT 'Amit Sharma', '9988776655', 'amit@gmail.com', 'Pune'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE phone = '9988776655');

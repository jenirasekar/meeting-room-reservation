-- Meeting Room Reservation System Database Schema
-- Run this script to set up the database

CREATE DATABASE IF NOT EXISTS meeting_room
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE meeting_room;

-- Users table
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Meeting rooms table
CREATE TABLE IF NOT EXISTS room (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    capacity INT NOT NULL DEFAULT 1,
    description TEXT,
    status ENUM('available', 'unavailable') NOT NULL DEFAULT 'available',
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Reservations table
CREATE TABLE IF NOT EXISTS reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('pending', 'approved', 'rejected', 'cancelled', 'completed') NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    INDEX idx_room_time (room_id, start_time, end_time),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Check-in table
CREATE TABLE IF NOT EXISTS checkin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method ENUM('manual', 'qr') NOT NULL DEFAULT 'manual',
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Reservation approval log
CREATE TABLE IF NOT EXISTS reservation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    admin_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Default admin user (password: admin123)
-- IMPORTANT: Generate a proper BCrypt hash before running, or register via the API first.
-- Use the Java code: BCrypt.hashpw("admin123", BCrypt.gensalt(12))
-- Example hash below (may not match — generate your own):
-- $2a$12$LJ3m4ys3Lk0TSwHCi8uKZu1QqJqQvBOEqvzJqWuOKWH5jpK7VpD5q
INSERT INTO user (username, password, email, role) VALUES
('admin', '$2a$12$TumDRGmDSlVpTm3v5pgY0OBJXZj9BH7Gbh7QUXt6YUf8ZQn7roazq', 'admin@meetingroom.com', 'admin'),
('user', '$2a$12$ITvp1/P96tjd6oAOxM5iduAiZMnnVJxXbgkLse/isw1E2DkV287pu', 'user@meetingroom.com', 'user');


-- Insert sample rooms
INSERT INTO room (name, location, capacity, description, status) VALUES
('Conference Room A', 'Floor 1, Building A', 20, 'Main conference room with projector', 'available'),
('Meeting Room B', 'Floor 2, Building A', 8, 'Small meeting room with whiteboard', 'available'),
('Board Room', 'Floor 3, Building B', 30, 'Large board room for executive meetings', 'available'),
('Huddle Room 1', 'Floor 1, Building B', 4, 'Quick huddle space', 'available');

-- Insert sample equipment
INSERT INTO equipment (room_id, name, quantity) VALUES
(1, 'Projector', 1),
(1, 'Whiteboard', 2),
(1, 'Conference Phone', 1),
(2, 'Whiteboard', 1),
(2, 'TV Screen', 1),
(3, 'Projector', 2),
(3, 'Conference Phone', 2),
(3, 'Video Conferencing System', 1),
(4, 'Whiteboard', 1);

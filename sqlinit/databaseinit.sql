CREATE DATABASE IF NOT EXISTS LibraryDB;
USE LibraryDB;

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'LEERLING') NOT NULL,
    actief_since_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    refresh_token VARCHAR(512)
);

-- Admin user (wachtwoord: admin321)
INSERT INTO user (name, email, password, role) 
VALUES ('Admin', 'admin@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN');
CREATE DATABASE IF NOT EXISTS LibraryDB;
USE LibraryDB;

CREATE TABLE IF NOT EXISTS school (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    lending_period_days INT NOT NULL,
    extend_period_days INT NOT NULL,
    max_extends INT NOT NULL,
    max_lends_student INT NOT NULL,
    max_lend_teacher INT NOT NULL,
    lender_notification_period INT NOT NULL,
    lender_late_notification_period INT NOT NULL,
    librarian_notification_not_returned INT NOT NULL,
    librarian_request_notification INT NOT NULL,
    external_visibility BOOLEAN NOT NULL,
    external_lending BOOLEAN NOT NULL,
    reviews BOOLEAN NOT NULL,
    review_moderation BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO school (
  name, address,
  lending_period_days, extend_period_days, max_extends,
  max_lends_student, max_lend_teacher,
  lender_notification_period, lender_late_notification_period,
  librarian_notification_not_returned, librarian_request_notification,
  external_visibility, external_lending,
  reviews, review_moderation
) VALUES (
  'Testschool', 'TestAddress 1',
  14, 7, 2,
  5, 10,
  3, 1,
  2, 1,
  true, true,
  true, false
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'LEERLING') NOT NULL,
    student_number VARCHAR(50),
    class VARCHAR(50), 
    school_id INT, 
    actief_since_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    must_change_password BOOLEAN DEFAULT FALSE,
    refresh_token VARCHAR(512),
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE SET NULL
);

-- Admin user (wachtwoord: admin321) of admin123 als dat veranderd is
INSERT INTO user (name, email, password, role, must_change_password) 
VALUES ('Admin', 'admin@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', FALSE);

CREATE TABLE IF NOT EXISTS campus (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    school_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

-- Default non-editable tags
INSERT INTO tag (name, is_default) VALUES ('Pedagogisch', true);
INSERT INTO tag (name, is_default) VALUES ('12-14j', true);
INSERT INTO tag (name, is_default) VALUES ('14-16j', true);
INSERT INTO tag (name, is_default) VALUES ('16-18j', true);
-- ...


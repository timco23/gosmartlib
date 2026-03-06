create database if not exists LibraryDB;
use LibraryDB;

-- Replace ReviewScore and ReviewCount later on with generated columns that calculate these values based on the SchoolItems that use this id
create table if not exists item 
(
		id int unsigned primary key not null auto_increment,
		title varchar(255) not null,
		publisher varchar(100) not null,
		author varchar(255) not null,
		isbn varchar(13) null,
		release_year smallint unsigned not null,
		book_language varchar(50) not null,
		page_count smallint unsigned not null,
		cover varchar(200) null,
		review_score decimal(2,1) check (review_score >= 0) not null,
		review_count int unsigned not null
);

create table if not exists school_item
(
		id int unsigned primary key not null auto_increment,
		school_id int unsigned not null,
		item_id int unsigned not null,
		tags varchar(100) not null, -- Will get replaced later on by a proper relation with Tag entities from a Tags table
		book_description text null,
		featured boolean not null,
		foreign key (item_id) references item(id),
		unique (item_id) -- So that a school can't have the same book twice
);

-- Testbook
insert into item(title,publisher,author,isbn,release_year,book_language,page_count,cover,review_score,review_count) values ('Het smelt','Das Mag','Lize Spit','9789492478092',2016,'Nederlands',480,'https://covers.openlibrary.org/b/isbn/9789492478092-L.jpg',4.2,1250);

CREATE TABLE IF NOT EXISTS school (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
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
    school_id BIGINT, 
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
    school_id BIGINT NOT NULL,
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

CREATE TABLE IF NOT EXISTS school_item_copy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_item_id INT,
    barcode VARCHAR(255),
    status VARCHAR(50),
    `condition` VARCHAR(50)
);
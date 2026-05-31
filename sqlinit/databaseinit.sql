SET NAMES utf8mb4;

create database if not exists LibraryDB;
use LibraryDB;

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
    reviews BOOLEAN NOT NULL,
    review_moderation BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    school_id BIGINT not null,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE CASCADE
);


create table if not exists item 
(
	id BIGINT primary key not null auto_increment,
	title varchar(255) not null,
	publisher varchar(100) null,
	author varchar(255) not null,
	isbn varchar(13) null,
	release_year smallint unsigned null,
	book_language varchar(50) not null,
	page_count smallint unsigned null,
	cover varchar(200) null,
	review_score decimal(2,1) check(review_score >= 0) not null,
	review_count int unsigned not null,
    book_description text not null,
    isUpdating boolean not null default false
);

create table if not exists class (
	id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    class_name varchar(100) not null,
    school_id BIGINT NOT NULL,
    foreign key (school_id) references school(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NULL,
    role ENUM('ADMIN', 'BIBLIOTHEEKBEHEERDER', 'LEERKRACHT', 'LEERLING') NOT NULL,
    student_number VARCHAR(50),
    smartschool_username VARCHAR(255) UNIQUE,
    school_id BIGINT,
    refresh_token VARCHAR(1024),
    smartschool_access_token VARCHAR(2048),
    oneroster_id varchar(255) unique,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE SET NULL,
    items_borrowed INT NOT NULL DEFAULT 0,
    deletion_requested_at TIMESTAMP NULL DEFAULT NULL,
    needs_password_change BOOLEAN NOT NULL DEFAULT FALSE
);


create table if not exists class_user (
	class_id BIGINT,
    user_id int,
    primary key(class_id, user_id),
    foreign key (class_id) references class(id) on delete cascade,
    foreign key (user_id) references user(id) on delete cascade
);


CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    description TEXT,
    stars TINYINT NOT NULL CHECK (stars >= 1 AND stars <= 5),
    created_at DATE NOT NULL,
    item_id BIGINT NOT NULL,
    user_id INT NOT NULL,
    approved BOOLEAN NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Admin users (default password: admin321 — change on first login via the setup wizard)
INSERT INTO user (name, first_name , email, password, role, school_id, smartschool_username, refresh_token, needs_password_change)
VALUES
('', "Admin" , 'admin@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', NULL, 'admin', NULL, TRUE),
('2', "Admin" , 'admin2@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', NULL, 'admin2', NULL, TRUE),
('3',"Admin" , 'admin3@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', NULL, 'admin3', NULL, TRUE);

-- table for the physical copies of a school item, with unique barcode and status
CREATE TABLE IF NOT EXISTS school_item_copy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT not null,
    school_id bigint not null,
    barcode VARCHAR(255),
    copy_status ENUM('BESCHIKBAAR', 'UITGELEEND','ONTBREKEND', 'KWIJT'),
    item_condition ENUM('NIEUW', 'GOED','GEBRUIKT','BESCHADIGD','ONGEDEFINIEERD'),
    featured boolean not null,
    campus_id bigint null,
    foreign key (item_id) references item(id) on delete restrict,
    foreign key (school_id) references school(id) on delete cascade,
    foreign key (campus_id) references campus(id) on delete set null
);


-- the main genre of a book
CREATE TABLE IF NOT EXISTS genre ( 
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO genre (name) VALUES 
('Fictie algemeen'),
('Literaire roman'),
('Spanning / thriller'),
('Detective / misdaad'),
('Fantasy'),
('Sciencefiction'),
('Dystopie'),
('Historische roman'),
('Romantiek'),
('Coming-of-age'),
('Avontuur'),
('Oorlog & conflict'),
('Horror'),
('Humor'),
('Graphic novel / strip'),
('Poëzie'),
('Non-fictie algemeen');

-- the subgenre of a book, linked to a main genre
CREATE TABLE IF NOT EXISTS subgenre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    genre_id BIGINT NOT NULL,
    FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
);

INSERT INTO subgenre (name, genre_id) VALUES 
-- Fictie algemeen (1)
('Kortverhaal', 1),
('Novelle', 1),

-- Literaire roman (2)
('Moderne literatuur', 2),
('Klassieke literatuur', 2),

-- Spanning / thriller (3)
('Psychologische thriller', 3),
('Juridische thriller', 3),
('Actiethriller', 3),

-- Detective / misdaad (4)
('Cozy mystery', 4),
('Noir', 4),
('Politieprocedure', 4),

-- Fantasy (5)
('High fantasy', 5),
('Urban fantasy', 5),
('Dark fantasy', 5),

-- Sciencefiction (6)
('Space opera', 6),
('Cyberpunk', 6),
('Hard sciencefiction', 6),

-- Dystopie (7)
('Post-apocalyptisch', 7),
('Klimaatfictie', 7),

-- Historische roman (8)
('Middeleeuwen', 8),
('Tweede Wereldoorlog', 8),
('Victoriaans', 8),

-- Romantiek (9)
('Hedendaagse romantiek', 9),
('Historische romantiek', 9),
('Paranormale romantiek', 9),

-- Coming-of-age (10)
('Jeugdroman', 10),
('Young adult', 10),

-- Avontuur (11)
('Reisavontuur', 11),
('Overlevingsverhaal', 11),

-- Oorlog & conflict (12)
('Oorlogsroman', 12),
('Antioorlogsliteratuur', 12),

-- Horror (13)
('Psychologische horror', 13),
('Bovennatuurlijke horror', 13),
('Gotische horror', 13),

-- Humor (14)
('Satire', 14),
('Absurdisme', 14),

-- Graphic novel / strip (15)
('Superhelden', 15),
('Manga', 15),
('Autobiografische strip', 15),

-- Poëzie (16)
('Sonnet', 16),
('Vrije vers', 16),
('Haiku', 16),

-- Non-fictie algemeen (17)
('Biografie / autobiografie', 17),
('Wetenschap & technologie', 17),
('Filosofie', 17),
('Maatschappij & politiek', 17),
('Psychologie', 17),
('Geschiedenis', 17),
('Kunst & cultuur', 17);

-- extra tags for a book
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO tag (name) VALUES 
('Liefde & relaties'),
('Vriendschap'),
('Identiteit & zelfbeeld'),
('Gender & seksualiteit'),
('Diversiteit & inclusie'),
('Mentale gezondheid'),
('Rouw & verlies'),
('Familie'),
('School & prestatiedruk'),
('Sociale media'),
('Migratie & afkomst'),
('Armoede & ongelijkheid'),
('Macht & onrecht'),
('Avontuur & ontdekking'),
('Overleven'),
('Toekomst & technologie');

-- junction table linking items to tags
CREATE TABLE IF NOT EXISTS item_tag (
    item_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (item_id, tag_id),
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- reading level of a book (A, B, C or D. increasing difficulty)
CREATE TABLE IF NOT EXISTS reading_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000) NOT NULL
);

INSERT INTO reading_level (name, description) VALUES 
('A', "Eenvoudig taalgebruik, korte zinnen, sterk verhalend, weinig abstractie"),
('B', "Gemiddeld taalgebruik, herkenbare structuren, beperkte complexiteit"),
('C', "Complexer, meerdere verhaallijnen of perspectieven, rijkere woordenschat"),
('D', "Literair, gelaagd, abstracte thema’s, impliciete betekenis");

-- didactic category of a book
CREATE TABLE IF NOT EXISTS didactic_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO didactic_category (name) VALUES 
('Lesmethodes'),
('Didactische handboeken'),
('Evaluatiemateriaal'),
('Naslagwerken');

CREATE TABLE IF NOT EXISTS reading_list (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    list_description TEXT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS reading_list_item (
	reading_list_id BIGINT,
    item_id BIGINT NOT NULL,
    PRIMARY KEY (reading_list_id, item_id),
    FOREIGN KEY (reading_list_id) REFERENCES reading_list(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS collection (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    collection_description TEXT NULL,
    school_id BIGINT NULL,
    featured boolean,
    globally_featured boolean,
    foreign key (school_id) references school(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS collection_item (
	collection_id BIGINT,
    item_id BIGINT NOT NULL,
    PRIMARY KEY (collection_id, item_id),
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_favorite_books (
    user_id INT NOT NULL,
    item_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_wishlisted_books (
    user_id INT NOT NULL,
    item_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

-- uitleningen van schoolitems aan gebruikers, met datum van uitlening en terugkeer, en status of het item is teruggebracht of niet
CREATE TABLE IF NOT EXISTS lending (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    borrower_id INT NULL,
    book_id INT NULL,
    school_id BIGINT NOT NULL,
    lending_date DATE,
    return_date DATE,
    returned BOOLEAN DEFAULT FALSE,
    date_returned DATE,
    extended BOOLEAN DEFAULT FALSE,
    extend_count INT NOT NULL DEFAULT 0,
    snap_class_name    VARCHAR(50),
    snap_campus_id     BIGINT,
    snap_campus_name   VARCHAR(100),
    snap_school_year   VARCHAR(9) NOT NULL,
    snap_reading_level VARCHAR(20),
    snap_book_title    VARCHAR(255),
    snap_book_genre    VARCHAR(50),
    snap_book_isbn     VARCHAR(20),
    FOREIGN KEY (borrower_id) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (book_id) REFERENCES school_item_copy(id) ON DELETE RESTRICT,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE CASCADE
);

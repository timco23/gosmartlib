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
    'AP Hogeschool Antwerpen', 'Noorderplaats 2',
    14, 7, 2,
    5, 10,
    3, 1,
    2, 1,
    true, true,
    true, false
);

CREATE TABLE IF NOT EXISTS campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    school_id BIGINT not null,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE CASCADE
);

-- Replace ReviewScore and ReviewCount later on with generated columns that calculate these values based on the SchoolItems that use this id
create table if not exists item 
(
	id int primary key not null auto_increment,
	title varchar(255) not null,
	publisher varchar(100) null,
	author varchar(255) not null,
	isbn varchar(13) null,
	release_year smallint unsigned null,
	book_language varchar(50) not null,
	page_count smallint unsigned null,
	cover varchar(200) null,
	review_score decimal(2,1) check (review_score >= 0) not null,
	review_count int unsigned not null,
    book_description text not null
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NULL,
    role ENUM('ADMIN', 'BIBLIOTHEEKBEHEERDER', 'LEERKRACHT', 'LEERLING') NOT NULL,
    student_number VARCHAR(50),
    class VARCHAR(50),
    smartschool_username VARCHAR(255) UNIQUE, 
    school_id BIGINT, 
    refresh_token VARCHAR(512),
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE SET NULL
);

-- Admin user (wachtwoord: admin321)
INSERT INTO user (name, email, password, role, school_id, smartschool_username, refresh_token) 
VALUES ('Admin', 'admin@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', 1, 'admin', NULL);


-- REMOVE AFTER TESTING ----------------------------------------------------------------------------------------------------------
-- CREATE TABLE IF NOT EXISTS tag (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(255) NOT NULL UNIQUE,
--     is_default BOOLEAN NOT NULL DEFAULT FALSE
-- );


-- Testbooks
insert into item(title,publisher,author,isbn,release_year,book_language,page_count,cover,review_score,review_count,book_description) values
('Het smelt','Das Mag','Lize Spit','9789492478092',2016,'Nederlands',480,'https://covers.openlibrary.org/b/isbn/9082410613-M.jpg',4.2,1250,'Een aangrijpende roman over trauma en jeugd.'),
('De donkere kamer van Damokles','Em. Querido','Willem Frederik Hermans','9789023413110',1958,'Nederlands',352,'https://covers.openlibrary.org/b/isbn/9023413113-M.jpg',4.4,876,'Een psychologische roman over identiteit en oorlog.'),
('De aanslag','De Bezige Bij','Harry Mulisch','9789023442448',1982,'Nederlands',256,'https://covers.openlibrary.org/b/isbn/902344244X-M.jpg',4.3,1543,'Een verhaal over schuld en verwerking na WOII.'),
('Turks fruit','Bezige Bij','Jan Wolkers','9789023403975',1969,'Nederlands',192,'https://covers.openlibrary.org/b/isbn/9023403975-M.jpg',3.9,654,'Een rauw liefdesverhaal met tragische elementen.'),
('De ontdekking van de hemel','De Bezige Bij','Harry Mulisch','9789023430988',1992,'Nederlands',944,'https://covers.openlibrary.org/b/isbn/9023430980-M.jpg',4.5,2103,'Een epische roman over wetenschap, religie en lot.'),
('Oorlogswinter','Leopold','Jan Terlouw','9789025861124',1972,'Nederlands',208,'https://covers.openlibrary.org/b/isbn/9025861121-M.jpg',4.1,3871,'Een jongen beleeft de hongerwinter tijdens WOII.'),
('The Great Gatsby','Scribner','F. Scott Fitzgerald','9780743273565',1925,'Engels',180,'https://covers.openlibrary.org/b/isbn/0743273567-M.jpg',4.0,52340,'Een klassiek verhaal over rijkdom en illusies.'),
('To Kill a Mockingbird','HarperCollins','Harper Lee','9780061935466',1960,'Engels',336,'https://covers.openlibrary.org/b/isbn/0061935468-M.jpg',4.6,89421,'Een roman over racisme en rechtvaardigheid.'),
('1984','Secker & Warburg','George Orwell','9780451524935',1949,'Engels',328,'https://covers.openlibrary.org/b/isbn/0451524934-M.jpg',4.7,112453,'Een dystopisch verhaal over totalitaire controle.'),
('The Catcher in the Rye','Little Brown','J.D. Salinger','9780316769174',1951,'Engels',224,'https://covers.openlibrary.org/b/isbn/0316769177-M.jpg',3.8,44217,'Een coming-of-age verhaal over vervreemding.'),
('Brave New World','Chatto & Windus','Aldous Huxley','9780060850524',1932,'Engels',311,'https://covers.openlibrary.org/b/isbn/0060850523-M.jpg',4.3,61892,'Een dystopie over technologie en controle.'),
('Sapiens','Harper','Yuval Noah Harari','9780062316097',2011,'Engels',443,'https://covers.openlibrary.org/b/isbn/0062316095-M.jpg',4.5,203451,'Een overzicht van de menselijke geschiedenis.'),
('Een kleine geschiedenis van de wereld','Lemniscaat','Ernst Gombrich','9789047700975',1935,'Nederlands',284,'https://covers.openlibrary.org/b/isbn/904770097X-M.jpg',4.4,1234,'Een toegankelijke wereldgeschiedenis.'),
('Het korte geheugen','Prometheus','Maarten van Rossem','9789044628180',2019,'Nederlands',256,'https://covers.openlibrary.org/b/isbn/9044628186-M.jpg',3.7,423,'Reflecties op geschiedenis en politiek.'),
('Le Petit Prince','Gallimard','Antoine de Saint-Exupéry','9782070408504',1943,'Frans',96,'https://covers.openlibrary.org/b/isbn/2070408507-M.jpg',4.8,178932,'Een filosofisch sprookje over het leven.'),
('L''Étranger','Gallimard','Albert Camus','9782070360024',1942,'Frans',186,'https://covers.openlibrary.org/b/isbn/2070360024-M.jpg',4.3,88321,'Een existentiële roman over absurditeit.'),
('Der Vorleser','Diogenes','Bernhard Schlink','9783257229530',1995,'Duits',208,'https://covers.openlibrary.org/b/isbn/3257229534-M.jpg',4.1,32109,'Een verhaal over schuld en liefde na WOII.'),
('Die Verwandlung','Kurt Wolff Verlag','Franz Kafka','9783150090299',1915,'Duits',96,'https://covers.openlibrary.org/b/isbn/3150090296-M.jpg',4.2,45678,'Een man verandert plots in een insect.'),
('Het dagboek van een muts','Van Goor','Jeff Kinney','9789047514527',2007,'Nederlands',224,'https://covers.openlibrary.org/b/isbn/9047514521-M.jpg',4.0,9823,'Humoristisch dagboek van een tiener.'),
('Harry Potter en de Steen der Wijzen','Uitgeverij De Harmonie','J.K. Rowling','9789061699552',1997,'Nederlands',336,'https://covers.openlibrary.org/b/isbn/906169955X-M.jpg',4.8,154320,'Een jongen ontdekt dat hij een tovenaar is.'),
('De schuld van de sterren','Prometheus','John Green','9789044628371',2012,'Nederlands',286,'https://covers.openlibrary.org/b/isbn/9044628372-M.jpg',4.3,67891,'Een liefdesverhaal tussen twee zieke tieners.');


-- REMOVE AFTER TESTING ----------------------------------------------------------------------------------------------------------
-- Koppeltabel die de school_item en tag tabellen nodig hebben voor de FK's
-- CREATE TABLE IF NOT EXISTS school_item_tag (
--     school_item_id INT UNSIGNED NOT NULL,
--     tag_id BIGINT NOT NULL,
--     PRIMARY KEY (school_item_id, tag_id),
--     FOREIGN KEY (school_item_id) REFERENCES school_item(id) ON DELETE CASCADE,
--     FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
-- );

-- REMOVE AFTER TESTING ----------------------------------------------------------------------------------------------------------
-- Koppel de 'Algemeen' tag aan alle school items
-- INSERT INTO school_item_tag (school_item_id, tag_id)
-- SELECT si.id, t.id
-- FROM school_item si, tag t
-- WHERE t.name = 'Algemeen';

-- table for the physical copies of a school item, with unique barcode and status
CREATE TABLE IF NOT EXISTS school_item_copy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT not null,
    school_id bigint not null,
    barcode VARCHAR(255),
    copy_status ENUM('BESCHIKBAAR', 'UITGELEEND','ONTBREKEND'),
    item_condition ENUM('NIEUW', 'GOED','GEBRUIKT','BESCHADIGD'),
    featured boolean not null,
    campus_id bigint null,
    foreign key (item_id) references item(id) on delete restrict,
    foreign key (school_id) references school(id) on delete cascade,
    foreign key (campus_id) references campus(id) on delete set null
);

INSERT INTO school_item_copy (school_id, item_id, barcode, copy_status, item_condition, featured, campus_id)
SELECT 1, id, 12345, 'BESCHIKBAAR', 'NIEUW', FALSE, NULL
FROM item;

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

-- reading level of a book (A, B, C or D. increasing difficulty)
CREATE TABLE IF NOT EXISTS reading_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO reading_level (name) VALUES 
('A'),
('B'),
('C'),
('D');

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
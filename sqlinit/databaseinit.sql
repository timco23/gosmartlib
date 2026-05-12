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
    book_description text not null
);

create table if not exists class (
	id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    class_name varchar(100) not null,
    school_id BIGINT NOT NULL,
    foreign key (school_id) references school(id) on delete cascade
);

-- Testclasses
INSERT INTO class (class_name, school_id) 
VALUES ("KlasA", 1), ("KlasB", 1);


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
    refresh_token VARCHAR(512),
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE SET NULL,
    items_borrowed INT NOT NULL DEFAULT 0
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
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Admin user (wachtwoord: admin321)
INSERT INTO user (name, email, password, role, school_id, smartschool_username, refresh_token) 
VALUES ('Admin', 'admin@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'ADMIN', 1, 'admin', NULL);

INSERT INTO user (name, first_name, email, password, role, school_id, smartschool_username, refresh_token)
VALUES
('Test', 'Leerling', 'leerling.dev@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'LEERLING', 1, NULL, NULL),
('Test', 'Leerkracht', 'leerkracht.dev@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'LEERKRACHT', 1, NULL, NULL),
('Test', 'Bibliotheekbeheerder', 'bibliotheekbeheerder.dev@gsmartlib.tech', '$2a$10$EsySx6V7it5x9EKH7dzU.e1ww0EZ96RwfSUwjTlpYizrjlz/AqYay', 'BIBLIOTHEEKBEHEERDER', 1, NULL, NULL);

-- Put student in classes KlasA and KlasB
insert into class_user (class_id, user_id) values (1, 2),(2,2);

-- Testbooks
insert into item(title,publisher,author,isbn,release_year,book_language,page_count,cover,review_score,review_count,book_description) values
('Het smelt','Das Mag','Lize Spit','9789492478092',2016,'Nederlands',480,'https://covers.openlibrary.org/b/isbn/9082410613-M.jpg',0,0,'Een aangrijpende roman over trauma en jeugd.'),
('De donkere kamer van Damokles','Em. Querido','Willem Frederik Hermans','9789023413110',1958,'Nederlands',352,'https://covers.openlibrary.org/b/isbn/9023413113-M.jpg',0,0,'Een psychologische roman over identiteit en oorlog.'),
('De aanslag','De Bezige Bij','Harry Mulisch','9789023442448',1982,'Nederlands',256,'https://covers.openlibrary.org/b/isbn/902344244X-M.jpg',0,0,'Een verhaal over schuld en verwerking na WOII.'),
('Turks fruit','Bezige Bij','Jan Wolkers','9789023403975',1969,'Nederlands',192,'https://covers.openlibrary.org/b/isbn/9023403975-M.jpg',0,0,'Een rauw liefdesverhaal met tragische elementen.'),
('De ontdekking van de hemel','De Bezige Bij','Harry Mulisch','9789023430988',1992,'Nederlands',944,'https://covers.openlibrary.org/b/isbn/9023430980-M.jpg',0,0,'Een epische roman over wetenschap, religie en lot.'),
('Oorlogswinter','Leopold','Jan Terlouw','9789025861124',1972,'Nederlands',208,'https://covers.openlibrary.org/b/isbn/9025861121-M.jpg',0,0,'Een jongen beleeft de hongerwinter tijdens WOII.'),
('The Great Gatsby','Scribner','F. Scott Fitzgerald','9780743273565',1925,'Engels',180,'https://covers.openlibrary.org/b/isbn/0743273567-M.jpg',0,0,'Een klassiek verhaal over rijkdom en illusies.'),
('To Kill a Mockingbird','HarperCollins','Harper Lee','9780061935466',1960,'Engels',336,'https://covers.openlibrary.org/b/isbn/0061935468-M.jpg',0,0,'Een roman over racisme en rechtvaardigheid.'),
('1984','Secker & Warburg','George Orwell','9780451524935',1949,'Engels',328,'https://covers.openlibrary.org/b/isbn/0451524934-M.jpg',0,0,'Een dystopisch verhaal over totalitaire controle.'),
('The Catcher in the Rye','Little Brown','J.D. Salinger','9780316769174',1951,'Engels',224,'https://covers.openlibrary.org/b/isbn/0316769177-M.jpg',0,0,'Een coming-of-age verhaal over vervreemding.'),
('Brave New World','Chatto & Windus','Aldous Huxley','9780060850524',1932,'Engels',311,'https://covers.openlibrary.org/b/isbn/0060850523-M.jpg',0,0,'Een dystopie over technologie en controle.'),
('Sapiens','Harper','Yuval Noah Harari','9780062316097',2011,'Engels',443,'https://covers.openlibrary.org/b/isbn/0062316095-M.jpg',0,0,'Een overzicht van de menselijke geschiedenis.'),
('Een kleine geschiedenis van de wereld','Lemniscaat','Ernst Gombrich','9789047700975',1935,'Nederlands',284,'https://covers.openlibrary.org/b/isbn/904770097X-M.jpg',0,0,'Een toegankelijke wereldgeschiedenis.'),
('Het korte geheugen','Prometheus','Maarten van Rossem','9789044628180',2019,'Nederlands',256,'https://covers.openlibrary.org/b/isbn/9044628186-M.jpg',0,0,'Reflecties op geschiedenis en politiek.'),
('Le Petit Prince','Gallimard','Antoine de Saint-Exupéry','9782070408504',1943,'Frans',96,'https://covers.openlibrary.org/b/isbn/2070408507-M.jpg',0,0,'Een filosofisch sprookje over het leven.'),
('L''Étranger','Gallimard','Albert Camus','9782070360024',1942,'Frans',186,'https://covers.openlibrary.org/b/isbn/2070360024-M.jpg',0,0,'Een existentiële roman over absurditeit.'),
('Der Vorleser','Diogenes','Bernhard Schlink','9783257229530',1995,'Duits',208,'https://covers.openlibrary.org/b/isbn/3257229534-M.jpg',0,0,'Een verhaal over schuld en liefde na WOII.'),
('Die Verwandlung','Kurt Wolff Verlag','Franz Kafka','9783150090299',1915,'Duits',96,'https://covers.openlibrary.org/b/isbn/3150090296-M.jpg',0,0,'Een man verandert plots in een insect.'),
('Het dagboek van een muts','Van Goor','Jeff Kinney','9789047514527',2007,'Nederlands',224,'https://covers.openlibrary.org/b/isbn/9047514521-M.jpg',0,0,'Humoristisch dagboek van een tiener.'),
('Harry Potter en de Steen der Wijzen','Uitgeverij De Harmonie','J.K. Rowling','9789061699552',1997,'Nederlands',336,'https://covers.openlibrary.org/b/isbn/906169955X-M.jpg',0,0,'Een jongen ontdekt dat hij een tovenaar is.'),
('De schuld van de sterren','Prometheus','John Green','9789044628371',2012,'Nederlands',286,'https://covers.openlibrary.org/b/isbn/9044628372-M.jpg',0,0,'Een liefdesverhaal tussen twee zieke tieners.');

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

INSERT INTO school_item_copy (school_id, item_id, barcode, copy_status, item_condition, featured, campus_id)
SELECT 1, id, CONCAT('BC', LPAD(id, 6, '0')), 'BESCHIKBAAR', 'NIEUW', FALSE, NULL
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

-- junction table linking items to tags
CREATE TABLE IF NOT EXISTS item_tag (
    item_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (item_id, tag_id),
    FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- Default tags for imported books
-- Tags: 1=Liefde & relaties, 2=Vriendschap, 3=Identiteit & zelfbeeld, 4=Gender & seksualiteit,
--       5=Diversiteit & inclusie, 6=Mentale gezondheid, 7=Rouw & verlies, 8=Familie,
--       9=School & prestatiedruk, 10=Sociale media, 11=Migratie & afkomst,
--       12=Armoede & ongelijkheid, 13=Macht & onrecht, 14=Avontuur & ontdekking,
--       15=Overleven, 16=Toekomst & technologie
-- Books (by insert order): 1=Het smelt, 2=De donkere kamer van Damokles, 3=De aanslag,
--   4=Turks fruit, 5=De ontdekking van de hemel, 6=Oorlogswinter, 7=The Great Gatsby,
--   8=To Kill a Mockingbird, 9=1984, 10=The Catcher in the Rye, 11=Brave New World,
--   12=Sapiens, 13=Een kleine geschiedenis van de wereld, 14=Het korte geheugen,
--   15=Le Petit Prince, 16=L'Étranger, 17=Der Vorleser, 18=Die Verwandlung,
--   19=Het dagboek van een muts, 20=Harry Potter en de Steen der Wijzen, 21=De schuld van de sterren
INSERT INTO item_tag (item_id, tag_id)
SELECT i.id, t.tag_id FROM (
    SELECT 1 AS seq, 1 AS tag_id UNION ALL  -- Het smelt: Liefde & relaties
    SELECT 1, 7 UNION ALL
    SELECT 1, 8 UNION ALL                    -- Familie
    SELECT 2, 3 UNION ALL                    -- De donkere kamer: Identiteit & zelfbeeld
    SELECT 2, 15 UNION ALL                   -- Overleven
    SELECT 3, 7 UNION ALL                    -- De aanslag: Rouw & verlies
    SELECT 3, 13 UNION ALL                   -- Macht & onrecht
    SELECT 4, 1 UNION ALL                    -- Turks fruit: Liefde & relaties
    SELECT 4, 7 UNION ALL                    -- Rouw & verlies
    SELECT 5, 16 UNION ALL                   -- De ontdekking van de hemel: Toekomst & technologie
    SELECT 5, 13 UNION ALL                   -- Macht & onrecht
    SELECT 6, 15 UNION ALL                   -- Oorlogswinter: Overleven
    SELECT 6, 8 UNION ALL                    -- Familie
    SELECT 7, 1 UNION ALL                    -- The Great Gatsby: Liefde & relaties
    SELECT 7, 12 UNION ALL                   -- Armoede & ongelijkheid
    SELECT 8, 13 UNION ALL                   -- To Kill a Mockingbird: Macht & onrecht
    SELECT 8, 5 UNION ALL                    -- Diversiteit & inclusie
    SELECT 9, 13 UNION ALL                   -- 1984: Macht & onrecht
    SELECT 9, 16 UNION ALL                   -- Toekomst & technologie
    SELECT 10, 3 UNION ALL                   -- The Catcher in the Rye: Identiteit & zelfbeeld
    SELECT 10, 9 UNION ALL                   -- School & prestatiedruk
    SELECT 11, 13 UNION ALL                  -- Brave New World: Macht & onrecht
    SELECT 11, 16 UNION ALL                  -- Toekomst & technologie
    SELECT 12, 5 UNION ALL                   -- Sapiens: Diversiteit & inclusie
    SELECT 12, 14 UNION ALL                  -- Avontuur & ontdekking
    SELECT 13, 14 UNION ALL                  -- Een kleine geschiedenis: Avontuur & ontdekking
    SELECT 14, 13 UNION ALL                  -- Het korte geheugen: Macht & onrecht
    SELECT 15, 3 UNION ALL                   -- Le Petit Prince: Identiteit & zelfbeeld
    SELECT 15, 2 UNION ALL                   -- Vriendschap
    SELECT 16, 3 UNION ALL                   -- L'Étranger: Identiteit & zelfbeeld
    SELECT 16, 6 UNION ALL                   -- Mentale gezondheid
    SELECT 17, 1 UNION ALL                   -- Der Vorleser: Liefde & relaties
    SELECT 17, 7 UNION ALL                   -- Rouw & verlies
    SELECT 18, 3 UNION ALL                   -- Die Verwandlung: Identiteit & zelfbeeld
    SELECT 18, 6 UNION ALL                   -- Mentale gezondheid
    SELECT 19, 2 UNION ALL                   -- Het dagboek van een muts: Vriendschap
    SELECT 19, 9 UNION ALL                   -- School & prestatiedruk
    SELECT 20, 2 UNION ALL                   -- Harry Potter: Vriendschap
    SELECT 20, 14 UNION ALL                  -- Avontuur & ontdekking
    SELECT 21, 1 UNION ALL                   -- De schuld van de sterren: Liefde & relaties
    SELECT 21, 7                             -- Rouw & verlies
) AS t
JOIN item i ON i.id = t.seq;

-- reading level of a book (A, B, C or D. increasing difficulty)
CREATE TABLE IF NOT EXISTS reading_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000) NOT NULL
);

INSERT INTO reading_level (name, description) VALUES 
('A', "eenvoudig taalgebruik, korte zinnen, sterk verhalend, weinig abstractie"),
('B', "gemiddeld taalgebruik, herkenbare structuren, beperkte complexiteit"),
('C', "complexer, meerdere verhaallijnen of perspectieven, rijkere woordenschat"),
('D', "literair, gelaagd, abstracte thema’s, impliciete betekenis");

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

-- uitleningen van schoolitems aan gebruikers, met datum van uitlening en terugkeer, en status of het item is teruggebracht of niet
CREATE TABLE IF NOT EXISTS lending (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    borrower_id INT NOT NULL,
    book_id INT NULL,
    school_id BIGINT NOT NULL,
    lending_date DATE,
    return_date DATE,
    returned BOOLEAN DEFAULT FALSE,
    date_returned DATE,
    extended BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (borrower_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (book_id) REFERENCES school_item_copy(id) ON DELETE RESTRICT,
    FOREIGN KEY (school_id) REFERENCES school(id) ON DELETE CASCADE
);


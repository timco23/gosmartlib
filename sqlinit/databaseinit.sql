SET NAMES utf8mb4;

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

-- Testbooks
insert into item(title,publisher,author,isbn,release_year,book_language,page_count,cover,review_score,review_count) values
('Het smelt','Das Mag','Lize Spit','9789492478092',2016,'Nederlands',480,null,4.2,1250),
('De donkere kamer van Damokles','Em. Querido','Willem Frederik Hermans','9789023413110',1958,'Nederlands',352,null,4.4,876),
('De aanslag','De Bezige Bij','Harry Mulisch','9789023442448',1982,'Nederlands',256,null,4.3,1543),
('Turks fruit','Bezige Bij','Jan Wolkers','9789023403975',1969,'Nederlands',192,null,3.9,654),
('De ontdekking van de hemel','De Bezige Bij','Harry Mulisch','9789023430988',1992,'Nederlands',944,null,4.5,2103),
('Oorlogswinter','Leopold','Jan Terlouw','9789025861124',1972,'Nederlands',208,null,4.1,3871),
('The Great Gatsby','Scribner','F. Scott Fitzgerald','9780743273565',1925,'Engels',180,null,4.0,52340),
('To Kill a Mockingbird','HarperCollins','Harper Lee','9780061935466',1960,'Engels',336,null,4.6,89421),
('1984','Secker & Warburg','George Orwell','9780451524935',1949,'Engels',328,null,4.7,112453),
('The Catcher in the Rye','Little Brown','J.D. Salinger','9780316769174',1951,'Engels',224,null,3.8,44217),
('Brave New World','Chatto & Windus','Aldous Huxley','9780060850524',1932,'Engels',311,null,4.3,61892),
('Sapiens','Harper','Yuval Noah Harari','9780062316097',2011,'Engels',443,null,4.5,203451),
('Een kleine geschiedenis van de wereld','Lemniscaat','Ernst Gombrich','9789047700975',1935,'Nederlands',284,null,4.4,1234),
('Het korte geheugen','Prometheus','Maarten van Rossem','9789044628180',2019,'Nederlands',256,null,3.7,423),
('Le Petit Prince','Gallimard','Antoine de Saint-Exupéry','9782070408504',1943,'Frans',96,null,4.8,178932),
('L''Étranger','Gallimard','Albert Camus','9782070360024',1942,'Frans',186,null,4.3,88321),
('Der Vorleser','Diogenes','Bernhard Schlink','9783257229530',1995,'Duits',208,null,4.1,32109),
('Die Verwandlung','Kurt Wolff Verlag','Franz Kafka','9783150090299',1915,'Duits',96,null,4.2,45678),
('Het dagboek van een muts','Van Goor','Jeff Kinney','9789047514527',2007,'Nederlands',224,null,4.0,9823),
('Harry Potter en de Steen der Wijzen','Uitgeverij De Harmonie','J.K. Rowling','9789061699552',1997,'Nederlands',336,null,4.8,154320),
('De schuld van de sterren','Prometheus','John Green','9789044628371',2012,'Nederlands',286,null,4.3,67891);

-- Voeg alle testbook items toe aan de schoolcatalogus van school 1
INSERT INTO school_item (school_id, item_id, tags, book_description, featured)
SELECT 
    1,
    id,
    'Algemeen',
    NULL,
    FALSE
FROM item;
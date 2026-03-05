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
		book_description varchar(1000) null,
		featured boolean not null,
		foreign key (item_id) references item(id),
		unique (item_id) -- So that a school can't have the same book twice
	);
    
    insert into item(title,publisher,author,isbn,release_year,book_language,page_count,cover,review_score,review_count) values ('Het smelt','Das Mag','Lize Spit','9789492478092',2016,'Nederlands',480,'https://covers.openlibrary.org/b/isbn/9789492478092-L.jpg',4.2,1250)
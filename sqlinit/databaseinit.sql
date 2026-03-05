	create database if not exists LibraryDB;
	use LibraryDB;

	-- Replace ReviewScore and ReviewCount later on with generated columns that calculate these values based on the SchoolItems that use this id
	create table if not exists Item 
	(
		Id int unsigned primary key not null auto_increment,
		Title varchar(255) not null,
		Publisher varchar(100) not null,
		Author varchar(255) not null,
		ISBN varchar(13) null,
		ReleaseYear smallint unsigned not null,
		BookLanguage varchar(50) not null,
		PageCount smallint unsigned not null,
		Cover varchar(200) null,
		ReviewScore decimal(2,1) check (ReviewScore >= 0) not null,
		ReviewCount int unsigned not null
	);

	create table if not exists School_Item
	(
		Id int unsigned primary key not null auto_increment,
		SchoolId int unsigned not null,
		ItemId int unsigned not null,
		Tags varchar(100) not null, -- Will get replaced later on by a proper relation with Tag entities from a Tags table
		BookDescription varchar(1000) null,
		Featured boolean not null,
		foreign key (ItemId) references Item(Id),
		unique (ItemId) -- So that a school can't have the same book twice
	);
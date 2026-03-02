create database if not exists LibraryDB;
use LibraryDB;

-- Later ReviewScore en ReviewCount vervangen met generated columns die hun waarde berekenen op basis van het aantal reviews met deze Id
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
    Tags varchar(100) not null, -- Zal later vervangen worden met fatsoenlijke relatie met Tags uit tags tabel
    Beschrijving varchar(1000) null,
    Uitgelicht boolean not null,
    foreign key (ItemId) references Item(Id)
);
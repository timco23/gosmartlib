use helloworlddb;

create table if not exists testtable
(
	Id int primary key not null auto_increment,
    Content varchar(255)
);

insert into testtable (Content) values ("hello world")
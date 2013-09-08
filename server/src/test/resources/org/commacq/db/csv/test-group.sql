create table TestTable (
  "id" varchar(20),
  "groupName" varchar(255),
  "superGroupName" varchar(255),
  "value" varchar(255)
);

insert into TestTable values ('1', 'group1', 'superGroup1', 'ABC');
insert into TestTable values ('2', 'group1', 'superGroup1', 'DEF');
insert into TestTable values ('3', 'group2', 'superGroup1', 'GHI');

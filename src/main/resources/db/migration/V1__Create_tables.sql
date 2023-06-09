create table LINKS_ALREADY_PROCESSED
(
    link varchar(2000)
);
create table LINKS_TO_BE_PROCESSED
(
    link varchar(2000)
);
create table NEWS
(
    ID          bigint auto_increment primary key,
    TITLE       text,
    CONTENT     text,
    URL         varchar(2000),
    CREATED_AT  TIMESTAMP,
    MODIFIED_AT TIMESTAMP
);

insert into LINKS_TO_BE_PROCESSED (LINK)
values ('https://sina.cn/');
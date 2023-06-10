create table links_already_processed
(
    link varchar(2000)
);
create table links_to_be_processed
(
    link varchar(2000)
);
create table news
(
    id          bigint auto_increment primary key,
    title       text,
    content     text,
    url         varchar(2000),
    created_at  timestamp default now(),
    modified_at timestamp default now()
);

insert into links_to_be_processed (link)
values ('https://sina.cn/');
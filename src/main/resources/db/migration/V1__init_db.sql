create table if not exists maboy
(
    id              uuid            constraint maboy_pk primary key,
    username        varchar(50)     not null unique,
    password        varchar(100)    not null,
    role            varchar(20)     not null
);
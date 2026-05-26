create table if not exists photo_card
(
    id                uuid          constraint photo_card_pk primary key,
    title             varchar(160)  not null,
    description       varchar(500),
    original_filename varchar(120)  not null,
    content_type      varchar(120)  not null,
    photo             blob          not null,
    created_at        timestamp     not null
);

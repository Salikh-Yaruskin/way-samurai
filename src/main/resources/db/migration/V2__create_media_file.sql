create table if not exists media_file
(
    id                  uuid            constraint media_file_pk primary key,
    original_filename   varchar(255)    not null,
    storage_key         varchar(255)    not null unique,
    content_type        varchar(100)    not null,
    size_bytes          bigint          not null,
    type                varchar(20)     not null,
    created_at          timestamp       not null
);

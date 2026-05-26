create table if not exists social_profile
(
    id                  uuid constraint social_profile_pk primary key,
    maboy_id            uuid         not null unique,
    display_name        varchar(100) not null,
    city                varchar(100),
    interests           varchar(1000),
    bio                 varchar(2000),
    avatar_filename     varchar(255),
    avatar_content_type varchar(100),
    avatar              blob,
    constraint social_profile_maboy_fk foreign key (maboy_id) references maboy (id)
);

create table if not exists friend_request
(
    id           uuid constraint friend_request_pk primary key,
    requester_id uuid        not null,
    receiver_id  uuid        not null,
    status       varchar(20) not null,
    created_at   timestamp   not null,
    answered_at  timestamp,
    constraint friend_request_requester_fk foreign key (requester_id) references maboy (id),
    constraint friend_request_receiver_fk foreign key (receiver_id) references maboy (id),
    constraint friend_request_users_check check (requester_id <> receiver_id)
);

create index if not exists friend_request_requester_idx on friend_request (requester_id);
create index if not exists friend_request_receiver_idx on friend_request (receiver_id);
create index if not exists friend_request_status_idx on friend_request (status);

create table if not exists shop_category
(
    id          uuid          constraint shop_category_pk primary key,
    name        varchar(120)  not null,
    slug        varchar(120)  not null unique,
    description varchar(500),
    active      boolean       not null
);

create table if not exists shop_product
(
    id                  uuid           constraint shop_product_pk primary key,
    name                varchar(160)   not null,
    sku                 varchar(80)    not null unique,
    price               numeric(12, 2) not null,
    stock_quantity      integer        not null,
    active              boolean        not null,
    primary_category_id uuid,
    constraint shop_product_primary_category_fk
        foreign key (primary_category_id) references shop_category (id)
);

create table if not exists shop_order
(
    id             uuid         constraint shop_order_pk primary key,
    customer_name  varchar(120) not null,
    customer_email varchar(160) not null,
    status         varchar(30)  not null,
    created_at     timestamp    not null
);

create table if not exists shop_product_category
(
    product_id  uuid not null,
    category_id uuid not null,
    constraint shop_product_category_pk primary key (product_id, category_id),
    constraint shop_product_category_product_fk
        foreign key (product_id) references shop_product (id) on delete cascade,
    constraint shop_product_category_category_fk
        foreign key (category_id) references shop_category (id) on delete cascade
);

create table if not exists shop_order_product
(
    order_id   uuid not null,
    product_id uuid not null,
    constraint shop_order_product_pk primary key (order_id, product_id),
    constraint shop_order_product_order_fk
        foreign key (order_id) references shop_order (id) on delete cascade,
    constraint shop_order_product_product_fk
        foreign key (product_id) references shop_product (id) on delete cascade
);

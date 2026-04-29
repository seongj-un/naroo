create table user_accounts (
    id varchar(36) primary key,
    login_id varchar(30) not null unique,
    password_hash varchar(255) not null,
    nickname varchar(20) not null,
    math_status varchar(32) not null,
    created_at datetime(6) not null
);

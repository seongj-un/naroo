create table diagnostic_starting_points (
    id varchar(36) primary key,
    user_id varchar(36) not null unique,
    selection_type varchar(32) not null,
    math_area varchar(64) not null,
    note varchar(200),
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint fk_diagnostic_starting_points_user
        foreign key (user_id) references user_accounts (id)
);

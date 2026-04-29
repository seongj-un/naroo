create table diagnostic_sessions (
    id varchar(36) primary key,
    user_id varchar(36) not null,
    starting_point_selection_id varchar(36) not null,
    math_area varchar(64) not null,
    status varchar(32) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint fk_diagnostic_sessions_user
        foreign key (user_id) references user_accounts (id),
    constraint fk_diagnostic_sessions_starting_point
        foreign key (starting_point_selection_id) references diagnostic_starting_points (id)
);

create index idx_diagnostic_sessions_user_created_at
    on diagnostic_sessions (user_id, created_at);

create table recovery_missions (
    id varchar(36) primary key,
    user_id varchar(36) not null,
    diagnostic_session_id varchar(36) not null,
    concept_tag varchar(100) not null,
    title varchar(120) not null,
    prompt varchar(500) not null,
    hints varchar(1000) not null,
    status varchar(32) not null,
    estimated_minutes int not null,
    created_at datetime(6) not null,
    completed_at datetime(6),
    constraint fk_recovery_missions_user
        foreign key (user_id) references user_accounts (id),
    constraint fk_recovery_missions_diagnostic_session
        foreign key (diagnostic_session_id) references diagnostic_sessions (id),
    constraint uq_recovery_missions_user_diagnostic_session
        unique (user_id, diagnostic_session_id)
);

create index idx_recovery_missions_user_created_at
    on recovery_missions (user_id, created_at);

alter table recovery_missions drop index uq_recovery_missions_user_diagnostic_session;

create index idx_recovery_missions_user_diagnostic_session
    on recovery_missions (user_id, diagnostic_session_id);

create table recovery_mission_submissions (
    id varchar(36) primary key,
    recovery_mission_id varchar(36) not null,
    user_id varchar(36) not null,
    answer_text varchar(1000) not null,
    feedback_title varchar(120) not null,
    feedback_message varchar(500) not null,
    next_action varchar(200) not null,
    submitted_at datetime(6) not null,
    constraint fk_recovery_mission_submissions_mission
        foreign key (recovery_mission_id) references recovery_missions (id),
    constraint fk_recovery_mission_submissions_user
        foreign key (user_id) references user_accounts (id),
    constraint uq_recovery_mission_submissions_mission
        unique (recovery_mission_id)
);

create index idx_recovery_mission_submissions_user_submitted_at
    on recovery_mission_submissions (user_id, submitted_at);

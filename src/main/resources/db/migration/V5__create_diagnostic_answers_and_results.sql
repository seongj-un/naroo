create table diagnostic_answers (
    id varchar(120) primary key,
    diagnostic_session_id varchar(36) not null,
    user_id varchar(36) not null,
    question_id varchar(100) not null,
    selected_choice_id varchar(32) not null,
    correct_choice_id varchar(32) not null,
    concept_tag varchar(100) not null,
    is_correct boolean not null,
    is_unknown boolean not null,
    answered_at datetime(6) not null,
    constraint fk_diagnostic_answers_session
        foreign key (diagnostic_session_id) references diagnostic_sessions (id),
    constraint fk_diagnostic_answers_user
        foreign key (user_id) references user_accounts (id),
    constraint uq_diagnostic_answers_session_question
        unique (diagnostic_session_id, question_id)
);

create index idx_diagnostic_answers_session
    on diagnostic_answers (diagnostic_session_id);

create table diagnostic_results (
    diagnostic_session_id varchar(36) primary key,
    user_id varchar(36) not null,
    math_area varchar(64) not null,
    total_question_count int not null,
    correct_count int not null,
    wrong_count int not null,
    unknown_count int not null,
    weak_links varchar(500) not null,
    primary_recovery_concept varchar(100) not null,
    summary varchar(500) not null,
    created_at datetime(6) not null,
    constraint fk_diagnostic_results_session
        foreign key (diagnostic_session_id) references diagnostic_sessions (id),
    constraint fk_diagnostic_results_user
        foreign key (user_id) references user_accounts (id)
);

create index idx_diagnostic_results_user_created_at
    on diagnostic_results (user_id, created_at);

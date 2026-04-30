alter table diagnostic_sessions
    add column question_snapshot_version int not null default 1;

create table diagnostic_session_questions (
    id varchar(160) primary key,
    diagnostic_session_id varchar(36) not null,
    question_id varchar(80) not null,
    math_area varchar(64) not null,
    prompt varchar(500) not null,
    correct_choice_id varchar(40) not null,
    concept_tag varchar(100) not null,
    display_order int not null,
    constraint fk_diagnostic_session_questions_session
        foreign key (diagnostic_session_id) references diagnostic_sessions (id),
    constraint uq_diagnostic_session_questions_session_question
        unique (diagnostic_session_id, question_id)
);

create table diagnostic_session_question_choices (
    id varchar(220) primary key,
    diagnostic_session_question_id varchar(160) not null,
    choice_id varchar(40) not null,
    text varchar(200) not null,
    display_order int not null,
    constraint fk_diagnostic_session_question_choices_question
        foreign key (diagnostic_session_question_id) references diagnostic_session_questions (id),
    constraint uq_diagnostic_session_question_choices_question_choice
        unique (diagnostic_session_question_id, choice_id)
);

create index idx_diagnostic_session_questions_session_order
    on diagnostic_session_questions (diagnostic_session_id, display_order);

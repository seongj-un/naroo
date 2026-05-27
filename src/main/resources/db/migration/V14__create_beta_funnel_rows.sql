create table beta_funnel_rows (
    diagnostic_session_id varchar(36) not null primary key,
    user_id varchar(36) not null,
    math_area varchar(64) null,
    question_snapshot_version int null,
    flow_variant varchar(100) null,
    result_copy_version varchar(100) null,
    login_succeeded_at datetime(6) null,
    starting_point_selected_at datetime(6) null,
    diagnostic_session_created_at datetime(6) null,
    first_question_shown_at datetime(6) null,
    first_answer_selected_at datetime(6) null,
    diagnostic_submitted_at datetime(6) null,
    trust_feedback_choice varchar(32) null,
    trust_feedback_at datetime(6) null,
    recovery_mission_created_at datetime(6) null,
    recovery_mission_submitted_at datetime(6) null,
    last_event_type varchar(64) not null,
    last_event_at datetime(6) not null,
    projected_at datetime(6) not null
);

create index idx_beta_funnel_rows_user_id on beta_funnel_rows (user_id);
create index idx_beta_funnel_rows_math_area on beta_funnel_rows (math_area);
create index idx_beta_funnel_rows_last_event_at on beta_funnel_rows (last_event_at);

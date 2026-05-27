create table beta_events (
    id varchar(36) not null primary key,
    event_type varchar(64) not null,
    user_id varchar(36) not null,
    diagnostic_session_id varchar(36) null,
    recovery_mission_id varchar(36) null,
    question_id varchar(120) null,
    math_area varchar(64) null,
    question_snapshot_version int null,
    flow_variant varchar(100) null,
    result_copy_version varchar(100) null,
    idempotency_key varchar(191) not null,
    payload_json text not null,
    occurred_at datetime(6) not null,
    received_at datetime(6) not null
);

create unique index uq_beta_events_idempotency_key on beta_events (idempotency_key);
create index idx_beta_events_event_type on beta_events (event_type);
create index idx_beta_events_user_id on beta_events (user_id);
create index idx_beta_events_diagnostic_session_id on beta_events (diagnostic_session_id);
create index idx_beta_events_occurred_at on beta_events (occurred_at);

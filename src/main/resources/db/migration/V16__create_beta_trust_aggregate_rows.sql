create table beta_trust_aggregate_rows (
    id varchar(255) not null primary key,
    math_area varchar(64) null,
    question_snapshot_version int null,
    flow_variant varchar(100) null,
    result_copy_version varchar(100) null,
    primary_recovery_concept varchar(120) null,
    feedback_count int not null,
    feels_right_count int not null,
    unsure_count int not null,
    last_event_at datetime(6) not null,
    projected_at datetime(6) not null
);

create index idx_beta_trust_aggregate_rows_math_area on beta_trust_aggregate_rows (math_area);
create index idx_beta_trust_aggregate_rows_snapshot_version on beta_trust_aggregate_rows (question_snapshot_version);
create index idx_beta_trust_aggregate_rows_flow_variant on beta_trust_aggregate_rows (flow_variant);

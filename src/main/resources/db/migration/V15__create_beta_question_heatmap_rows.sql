create table beta_question_heatmap_rows (
    id varchar(255) not null primary key,
    question_id varchar(120) not null,
    math_area varchar(64) null,
    concept_tag varchar(120) null,
    question_snapshot_version int null,
    flow_variant varchar(100) null,
    display_order int null,
    question_shown_count int not null,
    answer_selected_count int not null,
    unknown_answer_count int not null,
    abandoned_after_question_count int not null,
    last_event_at datetime(6) not null,
    projected_at datetime(6) not null
);

create index idx_beta_question_heatmap_rows_math_area on beta_question_heatmap_rows (math_area);
create index idx_beta_question_heatmap_rows_question_id on beta_question_heatmap_rows (question_id);
create index idx_beta_question_heatmap_rows_snapshot_version on beta_question_heatmap_rows (question_snapshot_version);

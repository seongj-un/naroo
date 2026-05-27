create table beta_tester_profiles (
    user_id varchar(36) not null primary key,
    cohort_tag varchar(64) null,
    target_match varchar(32) null,
    operator_note text null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null
);

create index idx_beta_tester_profiles_cohort_tag on beta_tester_profiles (cohort_tag);
create index idx_beta_tester_profiles_target_match on beta_tester_profiles (target_match);

alter table diagnostic_questions
    add column status varchar(32) not null default 'ACTIVE';

alter table recovery_mission_templates
    add column status varchar(32) not null default 'ACTIVE';

create index idx_diagnostic_questions_status_math_area_order
    on diagnostic_questions (status, math_area, display_order);

create index idx_recovery_mission_templates_status
    on recovery_mission_templates (status);

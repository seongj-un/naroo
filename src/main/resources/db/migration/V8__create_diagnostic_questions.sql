create table diagnostic_questions (
    id varchar(80) primary key,
    math_area varchar(64) not null,
    prompt varchar(500) not null,
    correct_choice_id varchar(40) not null,
    concept_tag varchar(100) not null,
    display_order int not null
);

create table diagnostic_question_choices (
    id varchar(140) primary key,
    question_id varchar(80) not null,
    choice_id varchar(40) not null,
    text varchar(200) not null,
    display_order int not null,
    constraint fk_diagnostic_question_choices_question
        foreign key (question_id) references diagnostic_questions (id),
    constraint uq_diagnostic_question_choices_question_choice
        unique (question_id, choice_id)
);

create index idx_diagnostic_questions_math_area_order
    on diagnostic_questions (math_area, display_order);

insert into diagnostic_questions (id, math_area, prompt, correct_choice_id, concept_tag, display_order) values
('equation-linear-1', 'EQUATION', '2x + 3 = 11일 때 x의 값은?', 'b', 'linear_equation', 1),
('equation-word-1', 'EQUATION', '어떤 수에 5를 더한 값이 그 수의 2배보다 1 작습니다. 어떤 수를 x라고 할 때 알맞은 식은?', 'a', 'equation_modeling', 2),
('function-substitution-1', 'FUNCTION', '함수 y = 2x + 1에서 x가 3일 때 y의 값은?', 'b', 'function_substitution', 1),
('function-slope-1', 'FUNCTION', '일차함수 y = -3x + 2의 기울기는?', 'a', 'linear_function_slope', 2),
('geometry-angle-1', 'GEOMETRY', '삼각형의 두 내각이 각각 50도, 60도일 때 나머지 한 내각은?', 'b', 'triangle_angle_sum', 1),
('geometry-coordinate-1', 'GEOMETRY', '점 (2, 3)을 x축 방향으로 4만큼 이동하면 새 좌표는?', 'a', 'coordinate_translation', 2),
('probability-basic-1', 'PROBABILITY_AND_STATISTICS', '동전을 한 번 던질 때 앞면이 나올 확률은?', 'a', 'basic_probability', 1),
('probability-counting-1', 'PROBABILITY_AND_STATISTICS', 'A, B, C 세 명을 한 줄로 세우는 방법의 수는?', 'b', 'permutation_counting', 2),
('sequence-pattern-1', 'SEQUENCE', '수열 2, 5, 8, 11, ...에서 다음에 올 수는?', 'b', 'arithmetic_sequence_pattern', 1),
('sequence-nth-1', 'SEQUENCE', '첫째항이 3이고 공차가 4인 등차수열의 셋째항은?', 'b', 'arithmetic_sequence_nth_term', 2);

insert into diagnostic_question_choices (id, question_id, choice_id, text, display_order) values
('equation-linear-1:a', 'equation-linear-1', 'a', '3', 1),
('equation-linear-1:b', 'equation-linear-1', 'b', '4', 2),
('equation-linear-1:c', 'equation-linear-1', 'c', '7', 3),
('equation-linear-1:unknown', 'equation-linear-1', 'unknown', '잘 모르겠음', 4),
('equation-word-1:a', 'equation-word-1', 'a', 'x + 5 = 2x - 1', 1),
('equation-word-1:b', 'equation-word-1', 'b', 'x - 5 = 2x + 1', 2),
('equation-word-1:c', 'equation-word-1', 'c', '5x = 2x - 1', 3),
('equation-word-1:unknown', 'equation-word-1', 'unknown', '잘 모르겠음', 4),
('function-substitution-1:a', 'function-substitution-1', 'a', '5', 1),
('function-substitution-1:b', 'function-substitution-1', 'b', '7', 2),
('function-substitution-1:c', 'function-substitution-1', 'c', '9', 3),
('function-substitution-1:unknown', 'function-substitution-1', 'unknown', '잘 모르겠음', 4),
('function-slope-1:a', 'function-slope-1', 'a', '-3', 1),
('function-slope-1:b', 'function-slope-1', 'b', '2', 2),
('function-slope-1:c', 'function-slope-1', 'c', '3', 3),
('function-slope-1:unknown', 'function-slope-1', 'unknown', '잘 모르겠음', 4),
('geometry-angle-1:a', 'geometry-angle-1', 'a', '60도', 1),
('geometry-angle-1:b', 'geometry-angle-1', 'b', '70도', 2),
('geometry-angle-1:c', 'geometry-angle-1', 'c', '80도', 3),
('geometry-angle-1:unknown', 'geometry-angle-1', 'unknown', '잘 모르겠음', 4),
('geometry-coordinate-1:a', 'geometry-coordinate-1', 'a', '(6, 3)', 1),
('geometry-coordinate-1:b', 'geometry-coordinate-1', 'b', '(2, 7)', 2),
('geometry-coordinate-1:c', 'geometry-coordinate-1', 'c', '(-2, 3)', 3),
('geometry-coordinate-1:unknown', 'geometry-coordinate-1', 'unknown', '잘 모르겠음', 4),
('probability-basic-1:a', 'probability-basic-1', 'a', '1/2', 1),
('probability-basic-1:b', 'probability-basic-1', 'b', '1/3', 2),
('probability-basic-1:c', 'probability-basic-1', 'c', '1', 3),
('probability-basic-1:unknown', 'probability-basic-1', 'unknown', '잘 모르겠음', 4),
('probability-counting-1:a', 'probability-counting-1', 'a', '3', 1),
('probability-counting-1:b', 'probability-counting-1', 'b', '6', 2),
('probability-counting-1:c', 'probability-counting-1', 'c', '9', 3),
('probability-counting-1:unknown', 'probability-counting-1', 'unknown', '잘 모르겠음', 4),
('sequence-pattern-1:a', 'sequence-pattern-1', 'a', '13', 1),
('sequence-pattern-1:b', 'sequence-pattern-1', 'b', '14', 2),
('sequence-pattern-1:c', 'sequence-pattern-1', 'c', '15', 3),
('sequence-pattern-1:unknown', 'sequence-pattern-1', 'unknown', '잘 모르겠음', 4),
('sequence-nth-1:a', 'sequence-nth-1', 'a', '7', 1),
('sequence-nth-1:b', 'sequence-nth-1', 'b', '11', 2),
('sequence-nth-1:c', 'sequence-nth-1', 'c', '15', 3),
('sequence-nth-1:unknown', 'sequence-nth-1', 'unknown', '잘 모르겠음', 4);

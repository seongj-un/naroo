# Diagnostic Validation Plan

This document defines how to validate Naroo's first 6-10 diagnostic questions before building too much product around them.

## Goal

Validate whether the diagnostic can identify where a student got stuck in math well enough that the student trusts the result.

The question is not:

> Did the student get a good score?

The question is:

> Does the student feel, "Yes, this describes where I am stuck, and I would try a 10-minute recovery mission tomorrow"?

## Target Participants

Find 5 students who match the first Naroo beta target:

- High-school student
- Fell behind after entering high school or after losing academy/study routine
- Currently avoids math or only survives school exams
- Does not know exactly where to restart

Avoid testing only with strong math students in the first round.

## Format

Use paper, Google Forms, Notion, or any simple form.

Do not build the full app before this test.

## Diagnostic Size

Use:

- 6-10 questions
- 3-5 weak-link concepts
- Short questions
- No long proofs
- No calculator dependence

Each question must map to at least one concept tag.

Example concept tags:

```text
equation_transform
linear_function
graph_reading
condition_parsing
symbol_comfort
```

## Test Script

Say this before the student starts:

```text
이건 시험이 아니고, 어디서부터 다시 시작하면 좋을지 찾는 진단이야.
모르는 문제는 찍지 말고 "잘 모르겠음"이라고 표시해줘.
```

Do not explain questions while the student takes the diagnostic.

## Data To Record

For each participant:

```text
participant_id:
grade:
math_status:
  - follows class
  - barely follows
  - mostly gave up
  - unknown
answers:
  question_id -> correct / wrong / unknown
weak_links_predicted:
  - concept 1
  - concept 2
  - concept 3
student_feedback:
  result_accuracy: 1-5
  would_try_10_min_routine: yes/no/maybe
  quote:
observer_notes:
```

Do not collect unnecessary personal data. Do not collect real names unless needed.

## Result Summary Template

After scoring, show a short result:

```text
전체가 무너진 게 아니에요.
지금은 이 3개 연결고리부터 다시 잡으면 돼요.

1. [weak link 1]
   [one plain-language reason]

2. [weak link 2]
   [one plain-language reason]

3. [weak link 3]
   [one plain-language reason]

내일 10분만 한다면 첫 복구 미션은 [concept]부터 시작하면 좋아요.
```

Avoid:

- "기초가 부족합니다."
- "오답이 많습니다."
- "수학 상을 다시 하세요."
- Long reports

## Feedback Questions

Ask these after showing the result:

1. "이 결과가 네가 수학에서 막힌 지점을 맞게 설명한다고 느껴?"
2. "틀렸다고 평가받는 느낌이 들었어, 아니면 시작 위치를 찾는 느낌이 들었어?"
3. "내일 10분짜리 복구 미션이 있으면 해볼 것 같아?"
4. "결과에서 이상하거나 기분 나쁜 표현이 있었어?"
5. "어떤 문제에서 바로 포기하고 싶었어?"

## Success Criteria

Build the beta if:

- 5 of 5 students complete the diagnostic, or at least 5 of 10 in a larger test.
- At least 3 of 5 say the result is mostly accurate.
- At least 3 of 5 say they would try a 10-minute routine.
- No participant says the result felt shaming or discouraging.

Do not build more platform features if:

- Students do not understand the questions.
- Students say the weak-link result feels wrong.
- Students feel judged or embarrassed by the result.
- Most students would not try the 10-minute routine.

## Question Quality Checklist

Each question should:

- Test one small idea.
- Map to a concept tag.
- Be answerable in under 60 seconds.
- Include "잘 모르겠음" as a valid option.
- Avoid trick wording.
- Avoid long arithmetic.
- Avoid school-specific printout context.

## After The Test

Create a short summary:

```text
Participants tested:
Completion rate:
Average result accuracy:
Would try 10-minute routine:
Top weak links:
Questions to remove:
Questions to rewrite:
Best student quote:
Decision:
  - build beta
  - revise diagnostic and retest
```

## Decision Rule

If at least 3 students trust the result and would try a 10-minute routine, build the beta.

If fewer than 3 students trust the result, fix the diagnostic before writing more UI or backend code.

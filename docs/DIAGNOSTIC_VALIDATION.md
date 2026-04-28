# Diagnostic Validation Plan

This document defines how to validate Naroo's first 6-10 diagnostic questions before building too much product around them.

## Goal

Validate whether the diagnostic can identify a low-pressure next step well enough that the student is willing to try a 10-minute recovery mission.

Naroo beta should not stop at a diagnostic result. The result must lead directly into the first recovery mission.

The question is not:

> Did the student get a good score?

It is also not:

> Will the student publicly admit, "Yes, this is where I am stuck"?

That question can trigger shame or defensiveness, especially if the founder asks directly.

The real question is:

> Does the student feel, "This next 10-minute mission feels like a place I could restart"?

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

After scoring, show a short next-step recommendation:

```text
전체가 무너진 게 아니에요.
다음 10분은 여기서 시작하면 부담이 가장 적어요.

1. [recommended start 1]
   [why this is a low-pressure restart point]

2. [recommended start 2]
   [why this helps the next mission]

3. [recommended start 3]
   [why this is connected to the student's answers]

첫 복구 미션은 [concept]부터 시작하면 좋아요.
```

Avoid:

- "기초가 부족합니다."
- "오답이 많습니다."
- "수학 상을 다시 하세요."
- "너는 여기서 막혔습니다."
- Long reports

## Feedback Questions

Ask these after showing the result:

1. "이 10분 미션부터 시작하면 부담이 덜할 것 같아?"
2. "이 결과를 친구가 봐도 기분 나쁘지 않을 것 같아?"
3. "너라면 다음 미션으로 뭐가 제일 덜 싫어?"
4. "틀렸다고 평가받는 느낌이 들었어, 아니면 시작 위치를 제안받는 느낌이 들었어?"
5. "어떤 문제에서 바로 포기하고 싶었어?"

## Success Criteria

Build the beta if:

- 5 of 5 students complete the diagnostic, or at least 5 of 10 in a larger test.
- At least 3 of 5 say the recommended 10-minute mission feels low-pressure enough to try.
- At least 3 of 5 can choose a next mission without seeming embarrassed or defensive.
- No participant says the result felt shaming or discouraging.

Do not build more platform features if:

- Students do not understand the questions.
- Students say the next-step recommendation feels random or unrelated.
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
Next-step acceptance:
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

If at least 3 students accept the next-step recommendation and would try a 10-minute routine, build the beta.

If fewer than 3 students would try the recommended next mission, fix the diagnostic/result framing before writing more UI or backend code.

# Naroo Beta Scope

This document defines what Naroo beta is allowed to build first, and what is intentionally deferred.

## Beta Goal

Naroo beta exists to validate one narrow claim:

> A student who gave up on high-school math can complete a short diagnostic, understand where they got stuck, and feel able to do one 10-minute recovery routine.

The beta is not a full math platform. It is a re-entry diagnostic.

## In Scope

### 1. High-School Math Re-Entry Diagnostic

- 5-minute diagnostic
- 6-10 hand-authored questions
- Deterministic scoring
- 2-3 weak-link result summary
- Question-set version tracking

### 2. First Recovery Routine

- One 10-minute routine after the diagnostic
- One weak concept at a time
- Hint-first feedback
- No final-answer dump
- Quiet mission tone

### 3. Student Account And Progress

- Social login
- Student account persistence
- Diagnostic attempt history
- Saved answers
- Saved result
- Next-session hook

### 4. Private Beta Distribution

- Private web link
- 5-10 target students
- No public launch
- No paid ads
- No school sales process yet

## Explicitly Not In Scope

### Full Grade Coverage From 초3 To 고등

Reason: Too broad for first validation. It expands curriculum, content, QA, and user segmentation before the narrow wedge works.

Return condition: Add lower grades only after high-school re-entry diagnostic shows completion, result trust, and next-day return.

### Advanced Or Top-Student Mode

Reason: Strong students already have more alternatives and different motivation. They are not the first pain point.

Return condition: Add advanced mode after core diagnostic/routine loop has repeat usage.

### School Printout Scanning

Reason: OCR, math notation parsing, teacher-specific formats, and homework-help behavior would pull Naroo toward Photomath-like competition.

Return condition: Add only after the product can explain weak links reliably without scanned input.

### Live AI Story Generation

Reason: It would make beta failures harder to debug. If students leave, we would not know whether the issue was diagnostic quality, story quality, latency, or AI output quality.

Return condition: Add after deterministic diagnostic and hand-authored recovery routine prove useful.

### Admin Question Editor

Reason: The first question set should change through code/resource review, not a CMS.

Return condition: Add after multiple question-set versions exist and non-developer content editing becomes a real bottleneck.

## Product Guardrails

- Diagnosis comes before story.
- Story supports persistence, not spectacle.
- The UI must never feel like a graded exam.
- The result summary must never shame the student.
- Every beta feature must help one of these metrics:
  - Diagnostic completion
  - Result trust
  - Recovery routine completion
  - Next-day return

## Beta Success Criteria

- 5 of 10 target students complete the diagnostic.
- 4 of 10 say the result describes where they are stuck.
- 3 of 10 complete the first recovery routine.
- 2 of 10 return the next day without being forced.

## Decision Rule

If a proposed feature does not improve the first diagnostic, weak-link result, or 10-minute recovery routine, defer it.

# TODOs

## Deferred scope for Naroo beta

Status: Done by docs/BETA_SCOPE.md on 2026-04-28.

What: Keep these features out of the first beta: full grade coverage from 초3-고등, advanced/top-student mode, school printout scanning, and live AI story generation.

Why: The approved beta wedge is a high-school math re-entry diagnostic. Pulling the long-term platform vision into v1 would slow validation and make failures harder to interpret.

Pros: Keeps the first implementation focused on diagnosis quality, completion, and next-day return.

Cons: The first beta will feel less like the full AI story platform vision.

Context: /office-hours and /plan-eng-review narrowed v1 to deterministic 5-minute diagnostic, 2-3 weak links, and a 10-minute recovery routine. These deferred features can return after the wedge works.

Depends on / blocked by: Beta usage evidence from 5-10 target students.

## Decide beta deployment target and environment strategy

Status: Done by docs/DEPLOYMENT_STRATEGY.md on 2026-04-28.

What: Choose where the private beta will run and how OAuth secrets, redirect URLs, and PostgreSQL connection settings will be managed.

Why: Social login and PostgreSQL make local-only testing insufficient for sharing a private link with classmates.

Pros: Enables real beta testing through a private web link.

Cons: Adds deployment, environment variable, OAuth app, and database setup work.

Context: The design doc calls for a private web link. /plan-eng-review selected social login plus PostgreSQL, so deployment needs to be planned before external testing.

Depends on / blocked by: OAuth provider choice and PostgreSQL hosting choice.

## Validate the first diagnostic questions with 5 students

Status: Done by docs/DIAGNOSTIC_VALIDATION.md on 2026-04-28.

What: Test the first 6-10 diagnostic questions on paper or in a simple form with 5 students who fell behind in high-school math.

Why: If the diagnostic is wrong, the product fails even if the API and database are correct.

Pros: Catches bad questions, weak concept tags, and confusing result summaries before building too much code.

Cons: Requires asking real students for time and feedback before coding feels complete.

Context: The /office-hours assignment says to show each student a 3-link result summary and ask whether it describes where they are stuck and whether they would do a 10-minute recovery mission tomorrow.

Depends on / blocked by: A first draft of the diagnostic question set.

## Create DESIGN.md from the approved beta UI direction

Status: Done by /design-review follow-up on 2026-04-28.

What: Add a repo-level DESIGN.md that captures Naroo's minimum design system: typography, color, spacing, radius, components, accessibility, and quiet mission tone.

Why: The approved design direction currently lives in the gstack plan artifact. A repo-level design system makes the implementation standard visible to future contributors and to /design-review.

Pros: Keeps the first UI consistent and prevents generic AI-app styling from slipping in.

Cons: Adds one more project document that must stay updated if the brand direction changes.

Context: /plan-design-review approved Pretendard or SUIT, warm neutral palette, 8px spacing, 44px touch targets, app UI over marketing hero, and quiet mission story tone.

Depends on / blocked by: None.

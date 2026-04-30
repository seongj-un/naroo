# Naroo Frontend Design Prompt

Use this document as the design prompt for Magic Patterns.

Design a mobile-first web app for **Naroo**, a math re-entry product for students who stopped following high-school math and want to restart without shame, pressure, or a test-like mood.

Naroo should feel like a quiet recovery mission. It should not feel like a fantasy RPG, a school worksheet, a generic AI tutor, or a SaaS landing page.

## Product Summary

Naroo helps a student:

1. Create a lightweight learning record.
2. Choose where math currently feels blocked.
3. Answer a few low-pressure diagnostic questions.
4. See weak links without harsh grading.
5. Start one 10-minute recovery mission.
6. Save progress and continue next time.

The design should communicate:

- "I can find where I got stuck."
- "This is not a test."
- "It is okay if I do not know."
- "I can finish one small thing today."

## Target User

The target user is a student who may already feel behind in math.

Design for:

- Low confidence
- Avoidance of test-like screens
- Short attention span
- Mobile use first
- A need for gentle progress, not motivation slogans

Do not design for:

- High-achiever dashboards
- Teacher/admin analytics as the first experience
- Marketing conversion pages
- Gamified fantasy progression

## Required Screens

Create the actual app experience, not a landing page.

Include these screens in the design:

```text
1. Entry
2. Signup / Login
3. Email Verification State
4. Learning Home
5. Starting Point Selection
6. Diagnostic Question Flow
7. Weak-Link Result
8. Recovery Mission
9. Mission Submission Feedback
10. Saved Progress / Next Session
11. Admin Content Management, as a secondary utility screen
```

The first screen should be usable immediately. Do not make a marketing hero page.

## Information Architecture

Use this flow:

```text
Entry
  -> Signup / Login
  -> Email Verification State, if needed
  -> Learning Home
  -> Starting Point Selection
  -> Diagnostic Question Flow
  -> Weak-Link Result
  -> Recovery Mission
  -> Mission Submission Feedback
  -> Learning Home / Saved Progress
```

Learning Home decides the user's next action:

- Email verification required
- Start diagnostic
- Create recovery mission
- Continue recovery mission

## Screen Requirements

### 1. Entry

Purpose: Start the student without making the app feel like a test.

Content:

- Naroo name or logo text
- One-line promise: `수학을 다시 시작할 위치를 5분 안에 찾기`
- Short reassurance: `점수 매기지 않음. 틀려도 계속 진행됨.`
- Primary CTA: `시작 위치 찾기`
- Secondary action: `이미 기록이 있어요`

Layout:

- App-like first screen, not a landing page
- No feature cards
- No testimonials
- No pricing or marketing sections

### 2. Signup / Login

Purpose: Let the student create or open a learning record.

Signup fields:

- Login ID
- Email
- Password
- Nickname
- Math status

Math status options:

- `수업을 따라가고 있어요`
- `간신히 따라가고 있어요`
- `거의 놓친 것 같아요`
- `아직 모르겠어요`

CTA:

- Signup: `내 기록 만들기`
- Login: `기록 불러오기`

Trust copy:

- `친구에게 보여줄 점수표를 만들지 않아요.`

### 3. Email Verification State

Purpose: Explain that learning can continue after email verification.

Content:

- Title: `이메일 확인이 필요해요`
- Body: `기록을 안전하게 저장하려면 이메일 인증을 먼저 완료해야 해요.`
- Token input or verification link state
- CTA: `인증 완료하기`
- Secondary: `나중에 다시 시도`

State copy:

- Loading: `인증을 확인하는 중...`
- Error: `인증을 완료하지 못했어요. 링크나 코드를 다시 확인해 주세요.`
- Success: `이메일 인증이 완료됐어요.`

### 4. Learning Home

Purpose: Show exactly one next action.

Content:

- Greeting using nickname
- Next action panel
- Latest diagnostic summary, if available
- Today's mission, if available
- Small progress line

Next action states:

- Email verification required: show verification CTA
- Start diagnostic: show starting point CTA
- Create recovery mission: show mission creation CTA
- Continue recovery mission: show mission resume CTA

Avoid:

- Dense dashboard grids
- Many competing cards
- Overly detailed analytics

### 5. Starting Point Selection

Purpose: Let the student choose where math currently feels blocked.

Question:

`요즘 수학에서 어디가 제일 막히나요?`

Choices:

- `식을 어떻게 바꿀지 모르겠어요`
- `함수 그래프가 나오면 막혀요`
- `도형 조건을 어디에 써야 할지 모르겠어요`
- `확률과 통계에서 기준을 못 잡겠어요`
- `수열 규칙을 식으로 못 바꾸겠어요`
- `어디서부터 다시 해야 할지 모르겠어요`

CTA:

- `가볍게 확인하기`

Use choice chips or full-width option rows. The selected state must be clear without relying only on color.

### 6. Diagnostic Question Flow

Purpose: Ask one small question at a time.

Required elements:

- Small progress indicator, for example `2 / 5`
- Concept label, for example `함수 그래프 읽기`
- One question prompt
- Answer choices
- Secondary option: `잘 모르겠어요`
- Reassurance line: `모름을 골라도 괜찮아요. 위치를 찾는 중이에요.`

Rules:

- One question per screen on mobile
- Full-width answer choices on mobile
- Do not show correct answer immediately
- Do not use harsh correct/wrong copy
- Submitting all answers should lead to the result screen

### 7. Weak-Link Result

Purpose: Show weak links without shaming the student.

Main result copy:

`전체가 무너진 게 아니에요. 여기부터 다시 연결하면 돼요.`

Required elements:

- Total question count
- Correct / wrong / unknown counts
- 2-3 weak links
- Primary recovery concept
- Short summary
- Next mission preview

CTA:

- Primary: `첫 10분 복습 시작`
- Secondary: `결과 저장하고 나중에 하기`

Do not create a score leaderboard or grade-like report.

### 8. Recovery Mission

Purpose: Give one 10-minute recovery routine.

Required elements:

- Mission title, for example `함수 그래프 읽기 10분 복구`
- Estimated time
- Concept explanation panel
- One tiny challenge
- Hints
- Answer text area
- Submit CTA

Copy:

- `아직 정답을 볼 필요는 없어요. 먼저 방향만 잡아볼게요.`
- `첫 단서는 x값이 커질 때 y가 어떻게 움직이는지예요.`

### 9. Mission Submission Feedback

Purpose: Confirm completion and guide the next small action.

Required elements:

- Feedback title
- Feedback message
- Completed mission status
- Next action
- CTA back to Learning Home

Copy:

- `오늘은 여기까지만 해도 충분해요.`
- `다음에는 식 변형부터 이어갈게요.`

### 10. Saved Progress / Next Session

Purpose: Make progress feel saved and resumable.

Required elements:

- Recent diagnostic summary
- In-progress mission, if any
- Completed mission count
- Next recommended action

Copy:

- `마지막 위치를 저장해뒀어요.`
- `오늘은 여기서 이어가면 돼요.`

### 11. Admin Content Management

Purpose: Secondary utility screen for managing diagnostic questions and recovery mission templates.

This screen should feel practical and quiet, not student-facing.

Required elements:

- Admin-only label
- Diagnostic question list
- Recovery mission template list
- Edit/upsert form
- Status control: `ACTIVE` / `INACTIVE`
- Save button

Important:

- Do not expose this screen as part of the main student flow.
- Admin access is based on `role === "ADMIN"`.

## Visual Direction

Use a calm, focused learning-tool aesthetic.

The interface should feel:

- Warm
- Quiet
- Clear
- Lightweight
- Trustworthy
- Slightly story-framed, but not fantasy-themed

The interface should not feel:

- Magical
- Game-like
- Competitive
- Corporate SaaS
- AI chatbot-first
- Dashboard-heavy

## Color

Use a calm neutral palette with one warm or natural accent.

Suggested direction:

- Background: warm off-white or very light gray
- Surface: white or near-white
- Main text: near-black, not pure black
- Muted text: accessible gray
- Border: soft neutral gray
- Accent: warm amber, muted green, or teal
- Error: calm red with recovery-oriented copy
- Success: soft green, used sparingly

Avoid:

- Purple/violet/indigo AI gradients
- One-note beige/brown palettes
- Dark blue/slate-dominant dashboards
- Decorative gradient blobs
- Bokeh/orb backgrounds

## Typography

Use a Korean-readable typeface.

Preferred fonts:

- Pretendard
- SUIT
- Noto Sans KR as fallback

Type scale:

- Body: 16px minimum
- Small helper text: 14px minimum
- H1 mobile: 28-32px
- H1 desktop: 40-48px
- H2 mobile: 22-24px
- H2 desktop: 28-32px
- Body line-height: 1.55
- Heading line-height: 1.2

Rules:

- Do not use negative letter spacing.
- Do not scale font size with viewport width.
- Numeric progress should use tabular numbers.
- Text must not overflow buttons, chips, cards, or panels.

## Layout

Mobile-first layout rules:

- Design for 375px width first.
- One main task per screen.
- No sidebars on mobile.
- Primary CTA near the lower comfortable thumb area.
- Answer choices full-width on mobile.
- Result screen shows max 3 weak links before CTA.
- Recovery mission shows one challenge at a time.

Tablet/desktop layout rules:

- Center the task column.
- Max reading width: 680px.
- Max task width: 760px.
- Optional right-side summary only at 1024px+.
- No dashboard-card mosaic in beta.

Spacing:

- Base spacing scale: 8px.
- Mobile page padding: 16px.
- Tablet page padding: 32px.
- Desktop page padding: 48px.
- Touch targets: 44px minimum.

## Shape And Surfaces

Cards are allowed only when the card is the real interaction.

Allowed panels:

- Question panel
- Result weak-link row
- Hint panel
- Recovery mission panel
- Admin edit form

Avoid:

- Decorative card grids
- Cards inside cards
- Floating card sections
- Overly rounded bubbly UI

Radius:

- Buttons: 8px
- Choice chips: 8px
- Panels: 8-12px
- Avoid 24px+ radius by default

## Components

Design these components:

- Primary button
- Secondary text button
- Login/signup form input
- Select control for math status
- Choice chip
- Full-width answer option
- Progress stepper
- Question panel
- Result weak-link row
- Hint panel
- Recovery mission panel
- Text area for mission answer
- Feedback state panel
- Admin status toggle or segmented control
- Admin edit form

Every interactive component needs:

- Hover state on desktop
- Visible focus state
- Pressed/active state
- Disabled state
- 44px minimum touch target

## Interaction States

Use these state labels and copy:

```text
Feature              | Loading                    | Empty                           | Error                                      | Success
---------------------|----------------------------|----------------------------------|--------------------------------------------|-------------------------------
Signup/Login          | 기록을 확인하는 중...       | 입력을 시작해 주세요              | 기록을 불러오지 못했어요. 다시 시도해 주세요 | 기록을 불러왔어요
Email verification    | 인증을 확인하는 중...       | 인증 코드나 링크가 필요해요       | 인증을 완료하지 못했어요                    | 이메일 인증이 완료됐어요
Learning home         | 오늘의 위치를 불러오는 중... | 아직 첫 기록이 없어요             | 학습 홈을 불러오지 못했어요                 | 다음 행동을 찾았어요
Starting point        | 첫 장면을 준비 중...        | 아직 몰라도 괜찮아요              | 선택을 저장하지 못했어요                    | 확인 질문으로 이동
Question flow         | 다음 문항을 불러오는 중...   | 문항을 찾지 못했어요              | 답변을 저장하지 못했어요                    | 다음 장면으로 이동
Weak-link result      | 지도 그리는 중...           | 결과가 아직 없어요                | 결과를 계산하지 못했어요                    | 첫 복습 시작 가능
Recovery mission      | 복습 장면 불러오는 중...     | 루틴을 찾지 못했어요              | 미션을 불러오지 못했어요                    | 10분 미션 완료
Saved progress        | 기록을 불러오는 중...        | 아직 저장된 기록이 없어요         | 기록을 불러오지 못했어요                    | 이어갈 위치를 찾았어요
Admin content         | 콘텐츠를 불러오는 중...      | 아직 콘텐츠가 없어요              | 저장하지 못했어요                           | 저장됐어요
```

## Accessibility

Requirements:

- Keyboard can tab through forms, chips, answers, and CTAs.
- Focus-visible ring must always be present.
- Buttons and chips use visible text labels.
- Color is never the only feedback for selected, error, success, or hint states.
- Body text contrast: 4.5:1 minimum.
- Large text contrast: 3:1 minimum.
- UI control contrast: 3:1 minimum.
- Use landmarks: header, main, nav, progress, section.
- Error text appears next to the failed action.
- Respect `prefers-reduced-motion`.

## Motion

Use motion only to clarify state changes.

Allowed:

- Short step transition between diagnostic questions
- Progress confirmation after answer submit
- Gentle success confirmation after mission completion

Avoid:

- Slow page transitions
- Decorative floating objects
- Motion that moves layout while the user is reading
- Animating width, height, top, or left

Use `transform` and `opacity` where possible.

## Voice And Microcopy

Tone: quiet mission.

Use:

- `전체가 무너진 게 아니에요.`
- `지금은 점수가 아니라 시작 위치를 찾는 중이에요.`
- `모른다고 눌러도 괜찮아요.`
- `오늘은 여기까지만 해도 충분해요.`
- `오늘의 복구 미션: 함수 그래프 읽기`
- `첫 단서는 x값이 커질 때 y가 어떻게 움직이는지예요.`
- `아직 정답을 볼 필요는 없어요. 먼저 방향만 잡아볼게요.`
- `오늘은 여기까지. 다음 장면은 식 변형이에요.`

Avoid:

- `오답입니다.`
- `기초가 부족합니다.`
- `다시 공부하세요.`
- `정답은...`
- `용사가 되어 수학왕국을 구하세요.`
- `마법의 함수 던전에 입장!`

## Hard Guardrails

Do not ship:

- Purple/violet/indigo gradient backgrounds
- Generic `AI-powered learning` hero copy
- Icon-in-circle feature grids
- Centered-everything marketing pages
- Uniform bubbly radius everywhere
- Decorative blobs, waves, or floating circles
- Emoji as primary design elements
- Colored left-border cards
- Hero -> features -> testimonials -> pricing -> CTA rhythm
- Scoreboard-like result pages
- Leaderboards
- Competitive streak pressure
- Student-facing admin controls

## Output Expectations

Produce a polished app UI that includes:

- Mobile-first screens for the full student flow
- Responsive desktop layout
- Clear component states
- Korean UI copy
- Practical admin content screen as a secondary route
- No marketing landing page
- No fantasy/RPG theme
- No generic AI visual language

The final design should look like a focused learning tool with a gentle story frame.

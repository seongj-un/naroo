# Naroo Design System

Naroo beta is a mobile-first math re-entry app for students who stopped following high-school math and need a low-pressure way to restart.

The interface should feel like a quiet mission, not a fantasy RPG, not a school worksheet, and not a generic AI app.

## Product Feel

Naroo should communicate:

- "I can find where I got stuck."
- "This is not a test."
- "It is okay if I do not know."
- "I can finish one small thing today."

Avoid:

- Harsh correct/wrong grading
- Big marketing hero sections
- Purple/blue AI gradients
- Three-card feature grids
- Fantasy RPG language
- Dashboard mosaics
- Decorative cards that are not real interactions

## UI Scope

The first beta should include these screens:

```text
Entry / Social Login
  -> Diagnostic Start
  -> Question Flow
  -> Weak-Link Result
  -> 10-Minute Recovery Routine
  -> Saved Progress / Next Session
```

## Information Architecture

Use story immersion lightly, but keep diagnosis as the spine.

```text
[Entry / Login]
  1. Naroo logo + one-line promise
     "수학을 다시 시작할 위치를 5분 안에 찾기"
  2. Story frame
     "첫 장면: 어디서부터 다시 시작할지 찾는 지도"
  3. Primary action
     "진단 시작하기"
  4. Trust line
     "점수 매기지 않음. 틀려도 계속 진행됨."
  5. Social login button
     "Google/Kakao로 계속하기"

[Diagnostic Start]
  1. Question: "언제부터 수학이 막혔나요?"
  2. Choice chips:
     - 고등학교 오자마자
     - 함수부터
     - 문제 첫 줄부터
     - 잘 모르겠음
  3. CTA: "5분 진단 시작"

[Question Flow]
  1. Progress: "3 / 8"
  2. Concept label: "함수 그래프 읽기"
  3. One small question
  4. Answer choices
  5. Secondary action: "잘 모르겠어요"
  6. Reassurance line: "모름을 골라도 괜찮아요. 위치를 찾는 중이에요."

[Weak-Link Result]
  1. Main result:
     "전체가 무너진 게 아니에요. 여기 3개가 약해요."
  2. Weak links:
     - 식 변형
     - 함수 그래프
     - 문제 조건 읽기
  3. Primary CTA:
     "첫 10분 복습 시작"
  4. Secondary:
     "결과 저장하고 나중에 하기"

[Recovery Routine]
  1. Mission title:
     "함수 그래프 읽기 10분 복구"
  2. One concept panel
  3. One tiny challenge
  4. Hint-first feedback
  5. End state:
     "오늘은 여기까지. 다음엔 식 변형부터."
```

## Typography

Use a Korean-readable typeface.

- Primary font: Pretendard or SUIT
- Body text: 16px minimum
- H1: 28-32px mobile, 40-48px desktop
- H2: 22-24px mobile, 28-32px desktop
- Body line-height: 1.55
- Heading line-height: 1.2
- Numeric progress: use tabular numbers

Do not use default system stacks as the final visual direction.

## Color

Use a calm neutral palette with one warm accent.

- Background: warm off-white or very light gray
- Main text: near-black, not pure black
- Muted text: accessible gray, body contrast >= 4.5:1
- Accent: warm amber, green, or teal
- Error: calm red with recovery copy
- Success: soft green, used sparingly

Do not use purple/indigo AI gradients as the main identity.

## Spacing And Layout

- Base spacing scale: 8px
- Mobile page padding: 16px
- Tablet page padding: 32px
- Desktop page padding: 48px
- Max reading width: 680px
- Max task width: 760px
- Touch targets: 44px minimum

Cards are allowed only when the card is the interaction:

- Question panel
- Result weak-link row
- Hint panel
- Recovery mission panel

Do not build decorative card grids.

## Radius

- Buttons: 8px
- Choice chips: 8px
- Question/routine panels: 12px max
- Avoid bubbly 24px+ radius by default

## Components

Minimum component vocabulary:

- Primary button
- Secondary text button
- Social login button
- Choice chip
- Progress stepper
- Question panel
- Result weak-link row
- Hint panel
- Recovery mission panel

Every interactive component needs:

- Hover state on desktop
- Visible focus state
- Pressed/active state
- Disabled state when unavailable
- 44px minimum touch target

## Interaction States

```text
FEATURE              | LOADING                  | EMPTY                         | ERROR                         | SUCCESS                       | PARTIAL
---------------------|--------------------------|-------------------------------|-------------------------------|-------------------------------|------------------------------
Social login          | "문을 여는 중..."        | 로그인 CTA만 노출             | "로그인에 실패했어요. 다시 시도" | "기록을 불러왔어요"          | 프로필 일부 없음: 닉네임만 표시
Diagnostic start      | "첫 장면을 준비 중..."   | 진단 기록 없음: "첫 여정 시작" | 질문셋 로드 실패: 재시도       | 시작 가능                     | 이전 시도 있음: 이어하기/새로하기
Question flow         | 다음 문항 로딩           | 문항 없음: 관리자 오류 메시지  | 답변 저장 실패: 다시 저장      | 다음 장면으로 이동            | "잘 모르겠어요" 선택 가능
Wrong answer          | 없음                     | 없음                          | 없음                          | 힌트 + 계속 진행              | 최종답 즉시 공개 금지
Weak-link result      | "지도 그리는 중..."      | 결과 없음: 진단 다시 시작      | 결과 계산 실패: 재시도         | 약점 2-3개 + 첫 복습 CTA      | 답변 부족: 신뢰도 낮음 표시
Recovery routine      | "복습 장면 불러오는 중"  | 루틴 없음: 약점 기반 fallback  | 루틴 로드 실패: 다른 루틴 제안 | 10분 완료 + 다음 세션 예고    | 중간 종료: 저장 후 이어하기
Saved progress        | 기록 로딩                | "아직 첫 기록이 없어요"        | 기록 불러오기 실패             | 최근 진단/다음 루틴 표시      | 어제 중단: 이어하기 CTA
```

## Mobile First

Viewport priority:

1. Mobile 375px
2. Mobile wide 430px
3. Tablet 768px
4. Desktop 1024px+

Mobile rules:

- One question per screen
- No sidebars
- Primary CTA near thumb zone
- Progress visible but small: "3 / 8"
- Answer choices full-width
- "잘 모르겠어요" is secondary but visible
- Result screen shows max 3 weak links before CTA
- Recovery routine shows one challenge at a time

Tablet/desktop rules:

- Center task column with max width 760px
- Optional right-side "오늘의 흐름" summary only on 1024px+
- No dashboard-card mosaic in beta

## Accessibility

- Keyboard can tab through login, chips, answers, and CTA
- Focus-visible ring must always be present
- Buttons and chips use visible text labels, not icon-only controls
- Color is never the only feedback for correct, wrong, or hint states
- Body text contrast: 4.5:1 minimum
- Large text contrast: 3:1 minimum
- UI control contrast: 3:1 minimum
- Use landmarks: header, main, progress, result
- Error text appears next to the failed action
- Respect `prefers-reduced-motion`

## Motion

Use motion only to clarify state changes.

Allowed:

- Scene transition between diagnostic steps
- Progress feedback after answer submit
- Gentle success confirmation after routine completion

Avoid:

- Slow page transitions
- Decorative floating objects
- Motion that moves layout while the user is reading
- Animating `width`, `height`, `top`, or `left`

Animate `transform` and `opacity` where possible.

## Voice And Microcopy

Tone: quiet mission.

Use:

- "전체가 무너진 게 아니에요."
- "지금은 점수가 아니라 시작 위치를 찾는 중이에요."
- "모른다고 눌러도 괜찮아요."
- "오늘은 여기까지만 해도 충분해요."
- "오늘의 복구 미션: 함수 그래프 읽기"
- "첫 단서는 x값이 커질 때 y가 어떻게 움직이는지예요."
- "아직 정답을 볼 필요는 없어요. 먼저 방향만 잡아볼게요."
- "오늘은 여기까지. 다음 장면은 식 변형이에요."

Avoid:

- "오답입니다."
- "기초가 부족합니다."
- "다시 공부하세요."
- "정답은..."
- "용사가 되어 수학왕국을 구하세요."
- "마법의 함수 던전에 입장!"

## AI Slop Guardrails

Do not ship:

- Purple/violet/indigo gradient backgrounds
- Generic "AI-powered learning" hero copy
- Icon-in-circle feature grids
- Centered-everything marketing pages
- Uniform bubbly radius everywhere
- Decorative blobs, waves, or floating circles
- Emoji as primary design elements
- Colored left-border cards
- Hero -> features -> testimonials -> pricing -> CTA rhythm

Naroo should look like a focused learning tool with a story frame, not a SaaS template.

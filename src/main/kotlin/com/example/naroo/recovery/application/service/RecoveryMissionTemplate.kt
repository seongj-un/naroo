package com.example.naroo.recovery.application.service

data class RecoveryMissionTemplate(
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val estimatedMinutes: Int = 10,
)

object RecoveryMissionTemplateCatalog {
    fun forConcept(conceptTag: String): RecoveryMissionTemplate {
        return templates[conceptTag] ?: RecoveryMissionTemplate(
            title = "$conceptTag 10분 복구 미션",
            prompt = "오늘은 이 개념에서 막힌 한 지점만 다시 확인해요. 정답보다 어디서 멈췄는지 찾는 게 목표예요.",
            hints = listOf(
                "문제를 풀기 전에 식이나 조건에서 아는 부분에 밑줄을 그어봐요.",
                "바로 계산하지 말고, 이 문제가 묻는 작은 개념이 무엇인지 먼저 말해봐요.",
                "모르면 멈춰도 괜찮아요. 멈춘 위치가 다음 시작점이에요.",
            ),
        )
    }

    private val templates = mapOf(
        "function_substitution" to RecoveryMissionTemplate(
            title = "함수값 대입 10분 복구 미션",
            prompt = "식에 x값을 넣고 y값이 어떻게 바뀌는지 한 줄씩 확인해요.",
            hints = listOf(
                "x가 들어간 자리에 주어진 숫자만 먼저 넣어봐요.",
                "곱셈과 덧셈 순서를 나눠서 한 줄씩 적어봐요.",
                "계산 결과가 y값이에요. 식 전체를 새로 외울 필요는 없어요.",
            ),
        ),
        "linear_function_slope" to RecoveryMissionTemplate(
            title = "일차함수 기울기 10분 복구 미션",
            prompt = "y = ax + b에서 기울기가 어떤 숫자인지 찾는 연습만 해요.",
            hints = listOf(
                "x 앞에 붙은 숫자를 먼저 찾아봐요.",
                "부호도 같이 봐야 해요. -3과 3은 방향이 달라요.",
                "상수항은 시작 높이일 뿐, 기울기 자체는 아니에요.",
            ),
        ),
        "linear_equation" to RecoveryMissionTemplate(
            title = "일차방정식 한 줄 정리 미션",
            prompt = "같은 항끼리 모아서 x가 혼자 남는 과정을 천천히 확인해요.",
            hints = listOf(
                "등호 양쪽에 같은 연산을 해도 균형은 유지돼요.",
                "숫자는 숫자끼리, x는 x끼리 모아봐요.",
                "마지막에는 x 앞의 숫자로 양쪽을 나누면 돼요.",
            ),
        ),
        "equation_modeling" to RecoveryMissionTemplate(
            title = "문장 식 세우기 10분 미션",
            prompt = "문장을 한 번에 식으로 만들지 말고, 기준이 되는 수를 먼저 정해요.",
            hints = listOf(
                "모르는 수를 x라고 정하고 문장에 표시해봐요.",
                "더한다, 뺀다, 두 배 같은 표현을 작은 식으로 바꿔봐요.",
                "같다는 표현이 나오는 곳이 등호가 들어갈 자리예요.",
            ),
        ),
        "triangle_angle_sum" to RecoveryMissionTemplate(
            title = "삼각형 각도 합 복구 미션",
            prompt = "삼각형의 세 각을 더하면 항상 180도라는 사실만 사용해요.",
            hints = listOf(
                "이미 아는 두 각을 먼저 더해봐요.",
                "180도에서 그 합을 빼면 남은 각이에요.",
                "그림이 달라도 삼각형이면 각의 합은 같아요.",
            ),
        ),
        "coordinate_translation" to RecoveryMissionTemplate(
            title = "좌표 이동 한 칸씩 확인 미션",
            prompt = "x축, y축 방향 이동이 좌표의 어느 숫자를 바꾸는지 분리해서 봐요.",
            hints = listOf(
                "x축 방향 이동은 첫 번째 숫자만 바꿔요.",
                "y축 방향 이동은 두 번째 숫자만 바꿔요.",
                "오른쪽과 위쪽은 더하고, 왼쪽과 아래쪽은 빼요.",
            ),
        ),
        "basic_probability" to RecoveryMissionTemplate(
            title = "기본 확률 분수 미션",
            prompt = "전체 경우 중 원하는 경우가 몇 개인지만 분수로 써봐요.",
            hints = listOf(
                "분모에는 전체 가능한 경우의 수를 써요.",
                "분자에는 원하는 결과의 수를 써요.",
                "동전 한 번은 앞면과 뒷면, 전체 2가지예요.",
            ),
        ),
        "permutation_counting" to RecoveryMissionTemplate(
            title = "순서 세기 10분 복구 미션",
            prompt = "자리를 하나씩 채운다고 생각하고 가능한 선택지를 곱해요.",
            hints = listOf(
                "첫 번째 자리에 올 수 있는 사람 수를 먼저 세요.",
                "한 명을 세우면 다음 자리는 선택지가 하나 줄어요.",
                "각 자리의 선택지를 곱하면 전체 방법 수예요.",
            ),
        ),
        "arithmetic_sequence_pattern" to RecoveryMissionTemplate(
            title = "등차 패턴 찾기 미션",
            prompt = "앞뒤 항의 차이가 일정한지 확인하고 다음 항을 이어 써요.",
            hints = listOf(
                "연속한 두 수의 차이를 먼저 계산해요.",
                "차이가 같다면 마지막 수에 그 차이를 더해요.",
                "패턴을 말로 설명하면 실수가 줄어요.",
            ),
        ),
        "arithmetic_sequence_nth_term" to RecoveryMissionTemplate(
            title = "등차수열 n번째 항 미션",
            prompt = "첫째항에서 공차를 몇 번 더해야 하는지 세어봐요.",
            hints = listOf(
                "셋째항은 첫째항에서 공차를 두 번 더한 값이에요.",
                "항 번호보다 더하는 횟수는 하나 적어요.",
                "먼저 둘째항을 구하고 셋째항으로 넘어가도 좋아요.",
            ),
        ),
    )
}

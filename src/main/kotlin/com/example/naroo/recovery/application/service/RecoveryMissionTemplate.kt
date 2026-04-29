package com.example.naroo.recovery.application.service

data class RecoveryMissionTemplate(
    val title: String,
    val prompt: String,
    val hints: List<String>,
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
    )
}

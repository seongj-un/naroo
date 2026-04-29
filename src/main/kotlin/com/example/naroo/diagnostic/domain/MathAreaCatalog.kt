package com.example.naroo.diagnostic.domain

data class MathAreaInfo(
    val code: MathArea,
    val name: String,
    val description: String,
    val recommendedFor: String,
    val displayOrder: Int,
)

object MathAreaCatalog {
    fun list(): List<MathAreaInfo> {
        return listOf(
            MathAreaInfo(
                code = MathArea.EQUATION,
                name = "방정식",
                description = "식 정리, 등식 변형, 해를 구하는 과정이 헷갈리는 경우",
                recommendedFor = "문제는 보이지만 식을 세우거나 풀 때 자주 막히는 학생",
                displayOrder = 1,
            ),
            MathAreaInfo(
                code = MathArea.FUNCTION,
                name = "함수",
                description = "그래프, 식, 대응 관계를 연결하는 부분이 어려운 경우",
                recommendedFor = "중등 후반부터 고등 수학 초반에서 자주 막히는 학생",
                displayOrder = 2,
            ),
            MathAreaInfo(
                code = MathArea.GEOMETRY,
                name = "도형",
                description = "도형 조건, 길이, 각도, 좌표 관계를 읽는 부분이 어려운 경우",
                recommendedFor = "그림은 보이지만 어떤 성질을 써야 할지 막히는 학생",
                displayOrder = 3,
            ),
            MathAreaInfo(
                code = MathArea.PROBABILITY_AND_STATISTICS,
                name = "확률과 통계",
                description = "경우의 수, 확률, 자료 해석에서 기준을 잡기 어려운 경우",
                recommendedFor = "조건을 나누거나 세는 방식이 자주 헷갈리는 학생",
                displayOrder = 4,
            ),
            MathAreaInfo(
                code = MathArea.SEQUENCE,
                name = "수열",
                description = "규칙, 일반항, 합 공식이 서로 연결되지 않는 경우",
                recommendedFor = "패턴은 보이지만 식으로 정리하는 순간 막히는 학생",
                displayOrder = 5,
            ),
        )
    }
}

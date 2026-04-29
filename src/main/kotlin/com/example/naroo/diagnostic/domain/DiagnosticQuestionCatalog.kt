package com.example.naroo.diagnostic.domain

data class DiagnosticQuestion(
    val id: DiagnosticQuestionId,
    val mathArea: MathArea,
    val prompt: String,
    val choices: List<DiagnosticQuestionChoice>,
    val correctChoiceId: DiagnosticQuestionChoiceId,
    val conceptTag: String,
    val displayOrder: Int,
)

@JvmInline
value class DiagnosticQuestionId(val value: String) {
    init {
        require(value.isNotBlank()) { "diagnosticQuestionId must not be blank" }
    }
}

data class DiagnosticQuestionChoice(
    val id: DiagnosticQuestionChoiceId,
    val text: String,
)

@JvmInline
value class DiagnosticQuestionChoiceId(val value: String) {
    init {
        require(value.isNotBlank()) { "diagnosticQuestionChoiceId must not be blank" }
    }
}

object DiagnosticQuestionCatalog {
    private val commonUnknownChoice = DiagnosticQuestionChoice(
        id = DiagnosticQuestionChoiceId("unknown"),
        text = "잘 모르겠음",
    )

    fun findByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.filter { it.mathArea == mathArea }.sortedBy { it.displayOrder }
    }

    private val questions = listOf(
        DiagnosticQuestion(
            id = DiagnosticQuestionId("equation-linear-1"),
            mathArea = MathArea.EQUATION,
            prompt = "2x + 3 = 11일 때 x의 값은?",
            choices = choices("3", "4", "7"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "linear_equation",
            displayOrder = 1,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("equation-word-1"),
            mathArea = MathArea.EQUATION,
            prompt = "어떤 수에 5를 더한 값이 그 수의 2배보다 1 작습니다. 어떤 수를 x라고 할 때 알맞은 식은?",
            choices = choices("x + 5 = 2x - 1", "x - 5 = 2x + 1", "5x = 2x - 1"),
            correctChoiceId = DiagnosticQuestionChoiceId("a"),
            conceptTag = "equation_modeling",
            displayOrder = 2,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("function-substitution-1"),
            mathArea = MathArea.FUNCTION,
            prompt = "함수 y = 2x + 1에서 x가 3일 때 y의 값은?",
            choices = choices("5", "7", "9"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "function_substitution",
            displayOrder = 1,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("function-slope-1"),
            mathArea = MathArea.FUNCTION,
            prompt = "일차함수 y = -3x + 2의 기울기는?",
            choices = choices("-3", "2", "3"),
            correctChoiceId = DiagnosticQuestionChoiceId("a"),
            conceptTag = "linear_function_slope",
            displayOrder = 2,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("geometry-angle-1"),
            mathArea = MathArea.GEOMETRY,
            prompt = "삼각형의 두 내각이 각각 50도, 60도일 때 나머지 한 내각은?",
            choices = choices("60도", "70도", "80도"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "triangle_angle_sum",
            displayOrder = 1,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("geometry-coordinate-1"),
            mathArea = MathArea.GEOMETRY,
            prompt = "점 (2, 3)을 x축 방향으로 4만큼 이동하면 새 좌표는?",
            choices = choices("(6, 3)", "(2, 7)", "(-2, 3)"),
            correctChoiceId = DiagnosticQuestionChoiceId("a"),
            conceptTag = "coordinate_translation",
            displayOrder = 2,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("probability-basic-1"),
            mathArea = MathArea.PROBABILITY_AND_STATISTICS,
            prompt = "동전을 한 번 던질 때 앞면이 나올 확률은?",
            choices = choices("1/2", "1/3", "1"),
            correctChoiceId = DiagnosticQuestionChoiceId("a"),
            conceptTag = "basic_probability",
            displayOrder = 1,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("probability-counting-1"),
            mathArea = MathArea.PROBABILITY_AND_STATISTICS,
            prompt = "A, B, C 세 명을 한 줄로 세우는 방법의 수는?",
            choices = choices("3", "6", "9"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "permutation_counting",
            displayOrder = 2,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("sequence-pattern-1"),
            mathArea = MathArea.SEQUENCE,
            prompt = "수열 2, 5, 8, 11, ...에서 다음에 올 수는?",
            choices = choices("13", "14", "15"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "arithmetic_sequence_pattern",
            displayOrder = 1,
        ),
        DiagnosticQuestion(
            id = DiagnosticQuestionId("sequence-nth-1"),
            mathArea = MathArea.SEQUENCE,
            prompt = "첫째항이 3이고 공차가 4인 등차수열의 셋째항은?",
            choices = choices("7", "11", "15"),
            correctChoiceId = DiagnosticQuestionChoiceId("b"),
            conceptTag = "arithmetic_sequence_nth_term",
            displayOrder = 2,
        ),
    )

    private fun choices(a: String, b: String, c: String): List<DiagnosticQuestionChoice> {
        return listOf(
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("a"), a),
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("b"), b),
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("c"), c),
            commonUnknownChoice,
        )
    }
}

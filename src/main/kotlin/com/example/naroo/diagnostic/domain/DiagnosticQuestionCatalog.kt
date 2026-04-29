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

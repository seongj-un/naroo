package com.example.naroo.content.port.`in`

import com.example.naroo.diagnostic.domain.MathArea

fun interface ListDiagnosticQuestionContentsUseCase {
    fun list(): List<DiagnosticQuestionContentResult>
}

data class DiagnosticQuestionContentResult(
    val id: String,
    val mathArea: MathArea,
    val prompt: String,
    val correctChoiceId: String,
    val conceptTag: String,
    val displayOrder: Int,
    val choices: List<DiagnosticQuestionChoiceContentResult>,
)

data class DiagnosticQuestionChoiceContentResult(
    val id: String,
    val text: String,
)

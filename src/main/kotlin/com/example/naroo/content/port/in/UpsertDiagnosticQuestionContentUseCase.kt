package com.example.naroo.content.port.`in`

import com.example.naroo.diagnostic.domain.MathArea

fun interface UpsertDiagnosticQuestionContentUseCase {
    fun upsert(command: UpsertDiagnosticQuestionContentCommand): DiagnosticQuestionContentResult
}

data class UpsertDiagnosticQuestionContentCommand(
    val id: String,
    val mathArea: MathArea,
    val prompt: String,
    val correctChoiceId: String,
    val conceptTag: String,
    val displayOrder: Int,
    val status: String,
    val choices: List<UpsertDiagnosticQuestionChoiceContentCommand>,
)

data class UpsertDiagnosticQuestionChoiceContentCommand(
    val id: String,
    val text: String,
)

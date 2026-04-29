package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea

fun interface GetDiagnosticQuestionsUseCase {
    fun get(command: GetDiagnosticQuestionsCommand): DiagnosticQuestionsResult
}

data class GetDiagnosticQuestionsCommand(
    val userId: String,
    val diagnosticSessionId: String,
)

data class DiagnosticQuestionsResult(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val questions: List<DiagnosticQuestionResult>,
)

data class DiagnosticQuestionResult(
    val id: String,
    val prompt: String,
    val choices: List<DiagnosticQuestionChoiceResult>,
)

data class DiagnosticQuestionChoiceResult(
    val id: String,
    val text: String,
)

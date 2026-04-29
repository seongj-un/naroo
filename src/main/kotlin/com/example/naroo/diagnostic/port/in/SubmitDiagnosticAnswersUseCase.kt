package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea

fun interface SubmitDiagnosticAnswersUseCase {
    fun submit(command: SubmitDiagnosticAnswersCommand): SubmittedDiagnosticResult
}

data class SubmitDiagnosticAnswersCommand(
    val userId: String,
    val diagnosticSessionId: String,
    val answers: List<SubmitDiagnosticAnswerCommand>,
)

data class SubmitDiagnosticAnswerCommand(
    val questionId: String,
    val selectedChoiceId: String,
)

data class SubmittedDiagnosticResult(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
)

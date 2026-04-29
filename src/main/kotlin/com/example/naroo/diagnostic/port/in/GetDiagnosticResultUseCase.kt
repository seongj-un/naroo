package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea

fun interface GetDiagnosticResultUseCase {
    fun get(command: GetDiagnosticResultCommand): DiagnosticResultView
}

data class GetDiagnosticResultCommand(
    val userId: String,
    val diagnosticSessionId: String,
)

data class DiagnosticResultView(
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
    val nextMissionPreview: NextMissionPreviewResult,
)

data class NextMissionPreviewResult(
    val conceptTag: String,
    val title: String,
    val estimatedMinutes: Int,
    val tone: String,
)

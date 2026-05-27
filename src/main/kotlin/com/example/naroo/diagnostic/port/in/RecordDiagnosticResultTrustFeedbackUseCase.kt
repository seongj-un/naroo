package com.example.naroo.diagnostic.port.`in`

import java.time.Instant

fun interface RecordDiagnosticResultTrustFeedbackUseCase {
    fun record(command: RecordDiagnosticResultTrustFeedbackCommand): RecordedDiagnosticResultTrustFeedbackResult
}

data class RecordDiagnosticResultTrustFeedbackCommand(
    val userId: String,
    val diagnosticSessionId: String,
    val feedbackChoice: DiagnosticResultTrustFeedbackChoice,
    val idempotencyKey: String,
    val occurredAt: Instant,
    val flowVariant: String? = null,
    val resultCopyVersion: String? = null,
)

data class RecordedDiagnosticResultTrustFeedbackResult(
    val diagnosticSessionId: String,
    val feedbackChoice: DiagnosticResultTrustFeedbackChoice,
    val outcome: DiagnosticTelemetryOutcome,
)

enum class DiagnosticResultTrustFeedbackChoice {
    FEELS_RIGHT,
    UNSURE,
}

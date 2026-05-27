package com.example.naroo.diagnostic.port.`in`

import java.time.Instant

fun interface RecordDiagnosticTelemetryUseCase {
    fun record(command: RecordDiagnosticTelemetryCommand): RecordedDiagnosticTelemetryResult
}

data class RecordDiagnosticTelemetryCommand(
    val userId: String,
    val diagnosticSessionId: String,
    val eventType: DiagnosticTelemetryEventType,
    val questionId: String,
    val idempotencyKey: String,
    val occurredAt: Instant,
    val selectedChoiceId: String? = null,
    val flowVariant: String? = null,
)

data class RecordedDiagnosticTelemetryResult(
    val diagnosticSessionId: String,
    val eventType: DiagnosticTelemetryEventType,
    val outcome: DiagnosticTelemetryOutcome,
)

enum class DiagnosticTelemetryEventType {
    QUESTION_SHOWN,
    ANSWER_SELECTED,
    SESSION_ABANDONED,
}

enum class DiagnosticTelemetryOutcome {
    APPENDED,
    DUPLICATE,
    SKIPPED,
}

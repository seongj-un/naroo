package com.example.naroo.diagnostic.application.service

import com.example.naroo.betaops.application.service.AppendBetaEventCommand
import com.example.naroo.betaops.application.service.AppendBetaEventOutcome
import com.example.naroo.betaops.application.service.AppendBetaEventService
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryEventType
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryOutcome
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticTelemetryCommand
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticTelemetryUseCase
import com.example.naroo.diagnostic.port.`in`.RecordedDiagnosticTelemetryResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class RecordDiagnosticTelemetryService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticSessionQuestionSnapshotRepositoryPort: DiagnosticSessionQuestionSnapshotRepositoryPort,
    private val appendBetaEventService: AppendBetaEventService,
) : RecordDiagnosticTelemetryUseCase {
    override fun record(command: RecordDiagnosticTelemetryCommand): RecordedDiagnosticTelemetryResult {
        val session = diagnosticSessionRepositoryPort.findById(DiagnosticSessionId(command.diagnosticSessionId))
            ?.takeIf { it.userId == UserId(command.userId) }
            ?: throw DiagnosticException.DiagnosticSessionNotFound

        val questions = diagnosticSessionQuestionSnapshotRepositoryPort.findByDiagnosticSessionId(session.id)
        if (questions.isEmpty()) {
            throw DiagnosticException.DiagnosticQuestionsNotFound
        }

        val question = questions.find { it.id == DiagnosticQuestionId(command.questionId) }
            ?: throw DiagnosticException.InvalidDiagnosticTelemetry

        val payload = mutableMapOf<String, Any?>(
            "questionDisplayOrder" to question.displayOrder,
            "conceptTag" to question.conceptTag,
            "eventSource" to "student_client",
        )

        if (command.eventType == DiagnosticTelemetryEventType.ANSWER_SELECTED) {
            val selectedChoiceId = command.selectedChoiceId
                ?.takeIf { it.isNotBlank() }
                ?.let(::DiagnosticQuestionChoiceId)
                ?: throw DiagnosticException.InvalidDiagnosticTelemetry
            if (question.choices.none { it.id == selectedChoiceId }) {
                throw DiagnosticException.InvalidDiagnosticTelemetry
            }
            payload["selectedChoiceId"] = selectedChoiceId.value
            payload["selectedUnknown"] = selectedChoiceId.value == "unknown"
        }

        val outcome = appendBetaEventService.append(
            AppendBetaEventCommand(
                eventType = command.eventType.toBetaEventType(),
                userId = command.userId,
                diagnosticSessionId = session.id.value,
                questionId = question.id.value,
                mathArea = session.mathArea,
                questionSnapshotVersion = session.questionSnapshotVersion,
                flowVariant = command.flowVariant?.trim()?.takeIf { it.isNotEmpty() },
                idempotencyKey = command.idempotencyKey,
                occurredAt = command.occurredAt,
                payload = payload,
            ),
        )

        return RecordedDiagnosticTelemetryResult(
            diagnosticSessionId = session.id.value,
            eventType = command.eventType,
            outcome = outcome.toDiagnosticTelemetryOutcome(),
        )
    }

    private fun DiagnosticTelemetryEventType.toBetaEventType(): BetaEventType {
        return when (this) {
            DiagnosticTelemetryEventType.QUESTION_SHOWN -> BetaEventType.DIAGNOSTIC_QUESTION_SHOWN
            DiagnosticTelemetryEventType.ANSWER_SELECTED -> BetaEventType.DIAGNOSTIC_ANSWER_SELECTED
            DiagnosticTelemetryEventType.SESSION_ABANDONED -> BetaEventType.DIAGNOSTIC_SESSION_ABANDONED
        }
    }

    private fun AppendBetaEventOutcome.toDiagnosticTelemetryOutcome(): DiagnosticTelemetryOutcome {
        return when (this) {
            AppendBetaEventOutcome.APPENDED -> DiagnosticTelemetryOutcome.APPENDED
            AppendBetaEventOutcome.DUPLICATE -> DiagnosticTelemetryOutcome.DUPLICATE
            AppendBetaEventOutcome.SKIPPED -> DiagnosticTelemetryOutcome.SKIPPED
        }
    }
}

package com.example.naroo.diagnostic.application.service

import com.example.naroo.betaops.application.service.AppendBetaEventCommand
import com.example.naroo.betaops.application.service.AppendBetaEventOutcome
import com.example.naroo.betaops.application.service.AppendBetaEventService
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryOutcome
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticResultTrustFeedbackCommand
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticResultTrustFeedbackUseCase
import com.example.naroo.diagnostic.port.`in`.RecordedDiagnosticResultTrustFeedbackResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class RecordDiagnosticResultTrustFeedbackService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticResultRepositoryPort: DiagnosticResultRepositoryPort,
    private val appendBetaEventService: AppendBetaEventService,
) : RecordDiagnosticResultTrustFeedbackUseCase {
    override fun record(command: RecordDiagnosticResultTrustFeedbackCommand): RecordedDiagnosticResultTrustFeedbackResult {
        if (command.idempotencyKey.isBlank()) {
            throw DiagnosticException.InvalidDiagnosticTrustFeedback
        }

        val session = diagnosticSessionRepositoryPort.findById(DiagnosticSessionId(command.diagnosticSessionId))
            ?.takeIf { it.userId == UserId(command.userId) }
            ?: throw DiagnosticException.DiagnosticSessionNotFound

        if (session.status != DiagnosticSessionStatus.COMPLETED) {
            throw DiagnosticException.DiagnosticResultNotReady
        }

        val result = diagnosticResultRepositoryPort.findByDiagnosticSessionId(session.id)
            ?: throw DiagnosticException.DiagnosticResultNotReady

        val outcome = appendBetaEventService.append(
            AppendBetaEventCommand(
                eventType = BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED,
                userId = command.userId,
                diagnosticSessionId = session.id.value,
                mathArea = session.mathArea,
                questionSnapshotVersion = session.questionSnapshotVersion,
                flowVariant = command.flowVariant?.trim()?.takeIf { it.isNotEmpty() },
                resultCopyVersion = command.resultCopyVersion?.trim()?.takeIf { it.isNotEmpty() },
                idempotencyKey = command.idempotencyKey,
                occurredAt = command.occurredAt,
                payload = mapOf(
                    "feedbackChoice" to command.feedbackChoice.name,
                    "primaryRecoveryConcept" to result.primaryRecoveryConcept,
                    "weakLinkCount" to result.weakLinks.size,
                ),
            ),
        )

        return RecordedDiagnosticResultTrustFeedbackResult(
            diagnosticSessionId = session.id.value,
            feedbackChoice = command.feedbackChoice,
            outcome = outcome.toDiagnosticTelemetryOutcome(),
        )
    }

    private fun AppendBetaEventOutcome.toDiagnosticTelemetryOutcome(): DiagnosticTelemetryOutcome {
        return when (this) {
            AppendBetaEventOutcome.APPENDED -> DiagnosticTelemetryOutcome.APPENDED
            AppendBetaEventOutcome.DUPLICATE -> DiagnosticTelemetryOutcome.DUPLICATE
            AppendBetaEventOutcome.SKIPPED -> DiagnosticTelemetryOutcome.SKIPPED
        }
    }
}

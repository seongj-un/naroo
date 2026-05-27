package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaFunnelRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaFunnelProjectionRepositoryPort
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class RefreshBetaFunnelProjectionService(
    private val betaEventReadPort: BetaEventReadPort,
    private val betaFunnelProjectionRepositoryPort: BetaFunnelProjectionRepositoryPort,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {
    fun refresh(): RefreshedBetaFunnelProjection {
        val events = betaEventReadPort.findAllOrderByOccurredAtAscReceivedAtAsc()
        val projectedAt = Instant.now(clock)
        val latestLoginByUser = mutableMapOf<String, Instant>()
        val latestStartingPointByUser = mutableMapOf<String, Instant>()
        val rowsBySessionId = linkedMapOf<String, MutableBetaFunnelRow>()

        events.forEach { event ->
            when (event.eventType) {
                BetaEventType.AUTH_LOGIN_SUCCEEDED -> {
                    latestLoginByUser[event.userId] = event.occurredAt
                }

                BetaEventType.DIAGNOSTIC_STARTING_POINT_SELECTED -> {
                    latestStartingPointByUser[event.userId] = event.occurredAt
                }

                else -> {
                    val diagnosticSessionId = event.diagnosticSessionId ?: return@forEach
                    val row = rowsBySessionId.getOrPut(diagnosticSessionId) {
                        MutableBetaFunnelRow(
                            diagnosticSessionId = diagnosticSessionId,
                            userId = event.userId,
                            loginSucceededAt = latestLoginByUser[event.userId],
                            startingPointSelectedAt = latestStartingPointByUser[event.userId],
                            mathArea = event.mathArea,
                            questionSnapshotVersion = event.questionSnapshotVersion,
                            flowVariant = event.flowVariant,
                            resultCopyVersion = event.resultCopyVersion,
                            lastEventType = event.eventType,
                            lastEventAt = event.occurredAt,
                        )
                    }
                    row.apply(event)
                }
            }
        }

        val rows = rowsBySessionId.values.map { it.toRow(projectedAt) }
        betaFunnelProjectionRepositoryPort.replaceAll(rows)

        val latestEventAt = events.maxOfOrNull { it.occurredAt }
        val lastProjectedEventAt = rows.maxOfOrNull { it.lastEventAt }
        return RefreshedBetaFunnelProjection(
            sourceEvents = events,
            rows = rows,
            latestEventAt = latestEventAt,
            lastProjectedEventAt = lastProjectedEventAt,
            projectedAt = projectedAt,
        )
    }

    private fun MutableBetaFunnelRow.apply(event: BetaEvent) {
        userId = event.userId
        mathArea = event.mathArea ?: mathArea
        questionSnapshotVersion = event.questionSnapshotVersion ?: questionSnapshotVersion
        flowVariant = event.flowVariant ?: flowVariant
        resultCopyVersion = event.resultCopyVersion ?: resultCopyVersion

        when (event.eventType) {
            BetaEventType.DIAGNOSTIC_SESSION_CREATED -> {
                diagnosticSessionCreatedAt = diagnosticSessionCreatedAt ?: event.occurredAt
            }

            BetaEventType.DIAGNOSTIC_QUESTION_SHOWN -> {
                firstQuestionShownAt = firstQuestionShownAt ?: event.occurredAt
            }

            BetaEventType.DIAGNOSTIC_ANSWER_SELECTED -> {
                firstAnswerSelectedAt = firstAnswerSelectedAt ?: event.occurredAt
            }

            BetaEventType.DIAGNOSTIC_SESSION_ABANDONED -> {
                firstQuestionShownAt = firstQuestionShownAt ?: event.occurredAt
            }

            BetaEventType.DIAGNOSTIC_ANSWERS_SUBMITTED -> {
                diagnosticSubmittedAt = diagnosticSubmittedAt ?: event.occurredAt
            }

            BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED -> {
                trustFeedbackAt = trustFeedbackAt ?: event.occurredAt
                trustFeedbackChoice = parseTrustFeedbackChoice(event.payloadJson) ?: trustFeedbackChoice
            }

            BetaEventType.RECOVERY_MISSION_CREATED -> {
                recoveryMissionCreatedAt = recoveryMissionCreatedAt ?: event.occurredAt
            }

            BetaEventType.RECOVERY_MISSION_SUBMITTED -> {
                recoveryMissionSubmittedAt = recoveryMissionSubmittedAt ?: event.occurredAt
            }

            else -> {
                // no-op
            }
        }

        if (event.occurredAt >= lastEventAt) {
            lastEventType = event.eventType
            lastEventAt = event.occurredAt
        }
    }

    private fun parseTrustFeedbackChoice(payloadJson: String): DiagnosticResultTrustFeedbackChoice? {
        return runCatching {
            val rawValue = objectMapper.readTree(payloadJson).path("feedbackChoice").takeIf { !it.isMissingNode }?.asText()
            rawValue
                ?.takeIf { it.isNotBlank() }
                ?.let(DiagnosticResultTrustFeedbackChoice::valueOf)
        }.getOrNull()
    }
}

data class RefreshedBetaFunnelProjection(
    val sourceEvents: List<BetaEvent>,
    val rows: List<BetaFunnelRow>,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant,
)

private data class MutableBetaFunnelRow(
    val diagnosticSessionId: String,
    var userId: String,
    var mathArea: com.example.naroo.diagnostic.domain.MathArea? = null,
    var questionSnapshotVersion: Int? = null,
    var flowVariant: String? = null,
    var resultCopyVersion: String? = null,
    var loginSucceededAt: Instant? = null,
    var startingPointSelectedAt: Instant? = null,
    var diagnosticSessionCreatedAt: Instant? = null,
    var firstQuestionShownAt: Instant? = null,
    var firstAnswerSelectedAt: Instant? = null,
    var diagnosticSubmittedAt: Instant? = null,
    var trustFeedbackChoice: DiagnosticResultTrustFeedbackChoice? = null,
    var trustFeedbackAt: Instant? = null,
    var recoveryMissionCreatedAt: Instant? = null,
    var recoveryMissionSubmittedAt: Instant? = null,
    var lastEventType: BetaEventType,
    var lastEventAt: Instant,
) {
    fun toRow(projectedAt: Instant): BetaFunnelRow {
        return BetaFunnelRow(
            diagnosticSessionId = diagnosticSessionId,
            userId = userId,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            loginSucceededAt = loginSucceededAt,
            startingPointSelectedAt = startingPointSelectedAt,
            diagnosticSessionCreatedAt = diagnosticSessionCreatedAt,
            firstQuestionShownAt = firstQuestionShownAt,
            firstAnswerSelectedAt = firstAnswerSelectedAt,
            diagnosticSubmittedAt = diagnosticSubmittedAt,
            trustFeedbackChoice = trustFeedbackChoice,
            trustFeedbackAt = trustFeedbackAt,
            recoveryMissionCreatedAt = recoveryMissionCreatedAt,
            recoveryMissionSubmittedAt = recoveryMissionSubmittedAt,
            lastEventType = lastEventType,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }
}

package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaFunnelRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaFunnelProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RefreshBetaFunnelProjectionServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T07:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `refresh projects diagnostic funnel stages into a row`() {
        val repository = CapturingBetaFunnelProjectionRepository()
        val service = RefreshBetaFunnelProjectionService(
            betaEventReadPort = FakeBetaEventReadPort(events()),
            betaFunnelProjectionRepositoryPort = repository,
            objectMapper = ObjectMapper(),
            clock = clock,
        )

        val result = service.refresh()

        assertEquals(1, result.rows.size)
        val row = repository.replaced.single()
        assertEquals("diagnostic-session-1", row.diagnosticSessionId)
        assertEquals(Instant.parse("2026-05-27T06:00:00Z"), row.loginSucceededAt)
        assertEquals(Instant.parse("2026-05-27T06:01:00Z"), row.startingPointSelectedAt)
        assertEquals(Instant.parse("2026-05-27T06:02:00Z"), row.diagnosticSessionCreatedAt)
        assertEquals(Instant.parse("2026-05-27T06:03:00Z"), row.firstQuestionShownAt)
        assertEquals(Instant.parse("2026-05-27T06:04:00Z"), row.firstAnswerSelectedAt)
        assertEquals(Instant.parse("2026-05-27T06:05:00Z"), row.diagnosticSubmittedAt)
        assertEquals(Instant.parse("2026-05-27T06:06:00Z"), row.trustFeedbackAt)
        assertEquals("FEELS_RIGHT", row.trustFeedbackChoice?.name)
        assertEquals(Instant.parse("2026-05-27T06:07:00Z"), row.recoveryMissionCreatedAt)
        assertEquals(Instant.parse("2026-05-27T06:08:00Z"), row.recoveryMissionSubmittedAt)
        assertEquals(BetaEventType.RECOVERY_MISSION_SUBMITTED, row.lastEventType)
        assertEquals(Instant.parse("2026-05-27T06:08:00Z"), row.lastEventAt)
        assertEquals(Instant.parse("2026-05-27T07:00:00Z"), row.projectedAt)
        assertEquals(Instant.parse("2026-05-27T06:08:00Z"), result.lastProjectedEventAt)
    }

    @Test
    fun `overview counts funnel stages and flags low sample`() {
        val service = GetBetaFunnelOverviewService(
            refreshBetaFunnelProjectionService = RefreshBetaFunnelProjectionService(
                betaEventReadPort = FakeBetaEventReadPort(events()),
                betaFunnelProjectionRepositoryPort = CapturingBetaFunnelProjectionRepository(),
                objectMapper = ObjectMapper(),
                clock = clock,
            ),
        )

        val result = service.get()

        assertEquals(1, result.loginUserCount)
        assertEquals(1, result.startingPointSelectedCount)
        assertEquals(1, result.diagnosticSessionCreatedCount)
        assertEquals(1, result.firstQuestionShownCount)
        assertEquals(1, result.firstAnswerSelectedCount)
        assertEquals(1, result.diagnosticSubmittedCount)
        assertEquals(1, result.trustFeedbackCount)
        assertEquals(1, result.trustFeelsRightCount)
        assertEquals(0, result.trustUnsureCount)
        assertEquals(1, result.recoveryMissionCreatedCount)
        assertEquals(1, result.recoveryMissionSubmittedCount)
        assertEquals(1.0, result.diagnosticSubmissionRate)
        assertEquals(1.0, result.trustFeedbackRate)
        assertTrue(result.lowSampleWarning)
        assertEquals(0L, result.projectionLagSeconds)
    }

    private fun events(): List<BetaEvent> {
        return listOf(
            event(BetaEventType.AUTH_LOGIN_SUCCEEDED, occurredAt = "2026-05-27T06:00:00Z"),
            event(BetaEventType.DIAGNOSTIC_STARTING_POINT_SELECTED, occurredAt = "2026-05-27T06:01:00Z"),
            event(
                BetaEventType.DIAGNOSTIC_SESSION_CREATED,
                diagnosticSessionId = "diagnostic-session-1",
                occurredAt = "2026-05-27T06:02:00Z",
            ),
            event(
                BetaEventType.DIAGNOSTIC_QUESTION_SHOWN,
                diagnosticSessionId = "diagnostic-session-1",
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T06:03:00Z",
                flowVariant = "beta-v1",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution"}""",
            ),
            event(
                BetaEventType.DIAGNOSTIC_ANSWER_SELECTED,
                diagnosticSessionId = "diagnostic-session-1",
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T06:04:00Z",
                payloadJson = """{"selectedChoiceId":"b","selectedUnknown":false}""",
            ),
            event(
                BetaEventType.DIAGNOSTIC_ANSWERS_SUBMITTED,
                diagnosticSessionId = "diagnostic-session-1",
                occurredAt = "2026-05-27T06:05:00Z",
            ),
            event(
                BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED,
                diagnosticSessionId = "diagnostic-session-1",
                occurredAt = "2026-05-27T06:06:00Z",
                resultCopyVersion = "result-copy-v1",
                payloadJson = """{"feedbackChoice":"FEELS_RIGHT"}""",
            ),
            event(
                BetaEventType.RECOVERY_MISSION_CREATED,
                diagnosticSessionId = "diagnostic-session-1",
                occurredAt = "2026-05-27T06:07:00Z",
            ),
            event(
                BetaEventType.RECOVERY_MISSION_SUBMITTED,
                diagnosticSessionId = "diagnostic-session-1",
                occurredAt = "2026-05-27T06:08:00Z",
            ),
        )
    }

    private fun event(
        eventType: BetaEventType,
        diagnosticSessionId: String? = null,
        questionId: String? = null,
        occurredAt: String,
        flowVariant: String? = null,
        resultCopyVersion: String? = null,
        payloadJson: String = "{}",
    ): BetaEvent {
        return BetaEvent(
            id = "$eventType:$occurredAt",
            eventType = eventType,
            userId = "user-1",
            diagnosticSessionId = diagnosticSessionId,
            questionId = questionId,
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            idempotencyKey = "$eventType:$occurredAt",
            payloadJson = payloadJson,
            occurredAt = Instant.parse(occurredAt),
            receivedAt = Instant.parse(occurredAt),
        )
    }
}

private class FakeBetaEventReadPort(
    private val events: List<BetaEvent>,
) : BetaEventReadPort {
    override fun findAllOrderByOccurredAtAscReceivedAtAsc(): List<BetaEvent> {
        return events
    }
}

private class CapturingBetaFunnelProjectionRepository : BetaFunnelProjectionRepositoryPort {
    var replaced: List<BetaFunnelRow> = emptyList()

    override fun replaceAll(rows: List<BetaFunnelRow>): List<BetaFunnelRow> {
        replaced = rows
        return rows
    }
}

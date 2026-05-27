package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaTrustAggregateRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaTrustAggregateProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RefreshBetaTrustAggregateProjectionServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T09:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `refresh aggregates trust feedback by variant and result copy`() {
        val repository = CapturingBetaTrustAggregateProjectionRepository()
        val service = RefreshBetaTrustAggregateProjectionService(
            betaEventReadPort = FakeTrustBetaEventReadPort(events()),
            betaTrustAggregateProjectionRepositoryPort = repository,
            objectMapper = ObjectMapper(),
            clock = clock,
        )

        val result = service.refresh()

        assertEquals(2, result.rows.size)
        val firstRow = repository.replaced.first { it.resultCopyVersion == "result-copy-v1" }
        assertEquals(2, firstRow.feedbackCount)
        assertEquals(1, firstRow.feelsRightCount)
        assertEquals(1, firstRow.unsureCount)
        assertEquals("linear_function_slope", firstRow.primaryRecoveryConcept)
        assertEquals(Instant.parse("2026-05-27T08:01:00Z"), firstRow.lastEventAt)
        assertEquals(Instant.parse("2026-05-27T09:00:00Z"), firstRow.projectedAt)
        assertEquals(Instant.parse("2026-05-27T08:02:00Z"), result.lastProjectedEventAt)
    }

    @Test
    fun `overview exposes trust rates and low sample warning`() {
        val service = GetBetaTrustAggregateService(
            refreshBetaTrustAggregateProjectionService = RefreshBetaTrustAggregateProjectionService(
                betaEventReadPort = FakeTrustBetaEventReadPort(events()),
                betaTrustAggregateProjectionRepositoryPort = CapturingBetaTrustAggregateProjectionRepository(),
                objectMapper = ObjectMapper(),
                clock = clock,
            ),
        )

        val result = service.get()

        val firstRow = result.rows.first { it.resultCopyVersion == "result-copy-v1" }
        assertEquals(2, result.rowCount)
        assertEquals(2, firstRow.feedbackCount)
        assertEquals(1, firstRow.feelsRightCount)
        assertEquals(1, firstRow.unsureCount)
        assertEquals(0.5, firstRow.feelsRightRate)
        assertEquals(0.5, firstRow.unsureRate)
        assertTrue(firstRow.lowSampleWarning)
    }

    private fun events(): List<BetaEvent> {
        return listOf(
            event(
                occurredAt = "2026-05-27T08:00:00Z",
                feedbackChoice = "FEELS_RIGHT",
                resultCopyVersion = "result-copy-v1",
            ),
            event(
                occurredAt = "2026-05-27T08:01:00Z",
                feedbackChoice = "UNSURE",
                resultCopyVersion = "result-copy-v1",
            ),
            event(
                occurredAt = "2026-05-27T08:02:00Z",
                feedbackChoice = "FEELS_RIGHT",
                resultCopyVersion = "result-copy-v2",
            ),
        )
    }

    private fun event(
        occurredAt: String,
        feedbackChoice: String,
        resultCopyVersion: String,
    ): BetaEvent {
        return BetaEvent(
            id = "trust:$occurredAt:$resultCopyVersion",
            eventType = BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED,
            userId = "user-1",
            diagnosticSessionId = "diagnostic-session-1",
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            flowVariant = "beta-v1",
            resultCopyVersion = resultCopyVersion,
            idempotencyKey = "trust:$occurredAt:$resultCopyVersion",
            payloadJson = """{"feedbackChoice":"$feedbackChoice","primaryRecoveryConcept":"linear_function_slope","weakLinkCount":1}""",
            occurredAt = Instant.parse(occurredAt),
            receivedAt = Instant.parse(occurredAt),
        )
    }
}

private class FakeTrustBetaEventReadPort(
    private val events: List<BetaEvent>,
) : BetaEventReadPort {
    override fun findAllOrderByOccurredAtAscReceivedAtAsc(): List<BetaEvent> {
        return events
    }
}

private class CapturingBetaTrustAggregateProjectionRepository : BetaTrustAggregateProjectionRepositoryPort {
    var replaced: List<BetaTrustAggregateRow> = emptyList()

    override fun replaceAll(rows: List<BetaTrustAggregateRow>): List<BetaTrustAggregateRow> {
        replaced = rows
        return rows
    }
}

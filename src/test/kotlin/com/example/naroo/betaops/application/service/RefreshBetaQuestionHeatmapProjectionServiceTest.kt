package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaQuestionHeatmapRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaQuestionHeatmapProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RefreshBetaQuestionHeatmapProjectionServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T08:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `refresh aggregates question heatmap counts from telemetry events`() {
        val repository = CapturingBetaQuestionHeatmapProjectionRepository()
        val service = RefreshBetaQuestionHeatmapProjectionService(
            betaEventReadPort = FakeHeatmapBetaEventReadPort(events()),
            betaQuestionHeatmapProjectionRepositoryPort = repository,
            objectMapper = ObjectMapper(),
            clock = clock,
        )

        val result = service.refresh()

        assertEquals(2, result.rows.size)
        val firstRow = repository.replaced.first { it.questionId == "function-substitution-1" }
        assertEquals(2, firstRow.questionShownCount)
        assertEquals(2, firstRow.answerSelectedCount)
        assertEquals(1, firstRow.unknownAnswerCount)
        assertEquals(1, firstRow.abandonedAfterQuestionCount)
        assertEquals("function_substitution", firstRow.conceptTag)
        assertEquals(1, firstRow.displayOrder)
        assertEquals(Instant.parse("2026-05-27T07:03:30Z"), firstRow.lastEventAt)
        assertEquals(Instant.parse("2026-05-27T08:00:00Z"), firstRow.projectedAt)
        assertEquals(Instant.parse("2026-05-27T07:05:00Z"), result.lastProjectedEventAt)
    }

    @Test
    fun `overview exposes rates and low sample flag per question`() {
        val service = GetBetaQuestionHeatmapService(
            refreshBetaQuestionHeatmapProjectionService = RefreshBetaQuestionHeatmapProjectionService(
                betaEventReadPort = FakeHeatmapBetaEventReadPort(events()),
                betaQuestionHeatmapProjectionRepositoryPort = CapturingBetaQuestionHeatmapProjectionRepository(),
                objectMapper = ObjectMapper(),
                clock = clock,
            ),
        )

        val result = service.get()

        val firstRow = result.rows.first { it.questionId == "function-substitution-1" }
        assertEquals(2, result.rowCount)
        assertEquals(2, firstRow.questionShownCount)
        assertEquals(2, firstRow.answerSelectedCount)
        assertEquals(1, firstRow.unknownAnswerCount)
        assertEquals(1, firstRow.abandonedAfterQuestionCount)
        assertEquals(1.0, firstRow.answerSelectionRate)
        assertEquals(0.5, firstRow.unknownSelectionRate)
        assertEquals(0.5, firstRow.abandonmentRate)
        assertTrue(firstRow.lowSampleWarning)
    }

    private fun events(): List<BetaEvent> {
        return listOf(
            event(
                eventType = BetaEventType.DIAGNOSTIC_QUESTION_SHOWN,
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T07:00:00Z",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution"}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_ANSWER_SELECTED,
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T07:01:00Z",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution","selectedUnknown":false}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_QUESTION_SHOWN,
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T07:02:00Z",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution"}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_ANSWER_SELECTED,
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T07:03:00Z",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution","selectedUnknown":true}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_SESSION_ABANDONED,
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T07:03:30Z",
                payloadJson = """{"questionDisplayOrder":1,"conceptTag":"function_substitution"}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_QUESTION_SHOWN,
                questionId = "function-slope-1",
                occurredAt = "2026-05-27T07:04:00Z",
                payloadJson = """{"questionDisplayOrder":2,"conceptTag":"linear_function_slope"}""",
            ),
            event(
                eventType = BetaEventType.DIAGNOSTIC_ANSWER_SELECTED,
                questionId = "function-slope-1",
                occurredAt = "2026-05-27T07:05:00Z",
                payloadJson = """{"questionDisplayOrder":2,"conceptTag":"linear_function_slope","selectedUnknown":false}""",
            ),
        )
    }

    private fun event(
        eventType: BetaEventType,
        questionId: String,
        occurredAt: String,
        payloadJson: String,
    ): BetaEvent {
        return BetaEvent(
            id = "$eventType:$questionId:$occurredAt",
            eventType = eventType,
            userId = "user-1",
            diagnosticSessionId = "diagnostic-session-1",
            questionId = questionId,
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            flowVariant = "beta-v1",
            idempotencyKey = "$eventType:$questionId:$occurredAt",
            payloadJson = payloadJson,
            occurredAt = Instant.parse(occurredAt),
            receivedAt = Instant.parse(occurredAt),
        )
    }
}

private class FakeHeatmapBetaEventReadPort(
    private val events: List<BetaEvent>,
) : BetaEventReadPort {
    override fun findAllOrderByOccurredAtAscReceivedAtAsc(): List<BetaEvent> {
        return events
    }
}

private class CapturingBetaQuestionHeatmapProjectionRepository : BetaQuestionHeatmapProjectionRepositoryPort {
    var replaced: List<BetaQuestionHeatmapRow> = emptyList()

    override fun replaceAll(rows: List<BetaQuestionHeatmapRow>): List<BetaQuestionHeatmapRow> {
        replaced = rows
        return rows
    }
}

package com.example.naroo.diagnostic.application.service

import com.example.naroo.betaops.application.service.AppendBetaEventService
import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryEventType
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryOutcome
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticTelemetryCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RecordDiagnosticTelemetryServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T06:00:00Z"), ZoneOffset.UTC)
    private val objectMapper = ObjectMapper()

    @Test
    fun `records question shown event for a diagnostic session question`() {
        val betaEventRepository = CapturingBetaEventRepository()
        val service = service(betaEventRepository)

        val result = service.record(
            RecordDiagnosticTelemetryCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
                eventType = DiagnosticTelemetryEventType.QUESTION_SHOWN,
                questionId = "function-substitution-1",
                idempotencyKey = "question-shown:1",
                occurredAt = Instant.parse("2026-05-27T05:59:00Z"),
                flowVariant = " beta-v1 ",
            ),
        )

        assertEquals(DiagnosticTelemetryOutcome.APPENDED, result.outcome)
        val saved = betaEventRepository.saved.single()
        assertEquals(BetaEventType.DIAGNOSTIC_QUESTION_SHOWN, saved.eventType)
        assertEquals("diagnostic-session-1", saved.diagnosticSessionId)
        assertEquals("function-substitution-1", saved.questionId)
        assertEquals(3, saved.questionSnapshotVersion)
        assertEquals("beta-v1", saved.flowVariant)
        assertTrue(saved.payloadJson.contains("\"questionDisplayOrder\":1"))
        assertTrue(saved.payloadJson.contains("\"conceptTag\":\"function_substitution\""))
    }

    @Test
    fun `records answer selected with selected choice payload`() {
        val betaEventRepository = CapturingBetaEventRepository()
        val service = service(betaEventRepository)

        val result = service.record(
            RecordDiagnosticTelemetryCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
                eventType = DiagnosticTelemetryEventType.ANSWER_SELECTED,
                questionId = "function-slope-1",
                selectedChoiceId = "unknown",
                idempotencyKey = "answer-selected:1",
                occurredAt = Instant.parse("2026-05-27T05:59:30Z"),
            ),
        )

        assertEquals(DiagnosticTelemetryOutcome.APPENDED, result.outcome)
        val saved = betaEventRepository.saved.single()
        assertEquals(BetaEventType.DIAGNOSTIC_ANSWER_SELECTED, saved.eventType)
        assertTrue(saved.payloadJson.contains("\"selectedChoiceId\":\"unknown\""))
        assertTrue(saved.payloadJson.contains("\"selectedUnknown\":true"))
    }

    @Test
    fun `rejects telemetry for missing or other users diagnostic session`() {
        val service = RecordDiagnosticTelemetryService(
            diagnosticSessionRepositoryPort = FakeTelemetryDiagnosticSessionRepository(diagnosticSession()),
            diagnosticSessionQuestionSnapshotRepositoryPort = FakeTelemetryQuestionSnapshotRepository(functionQuestions()),
            appendBetaEventService = appendService(CapturingBetaEventRepository()),
        )

        assertThrows(DiagnosticException.DiagnosticSessionNotFound::class.java) {
            service.record(
                RecordDiagnosticTelemetryCommand(
                    userId = "other-user",
                    diagnosticSessionId = "diagnostic-session-1",
                    eventType = DiagnosticTelemetryEventType.QUESTION_SHOWN,
                    questionId = "function-substitution-1",
                    idempotencyKey = "question-shown:forbidden",
                    occurredAt = Instant.parse("2026-05-27T05:59:00Z"),
                ),
            )
        }
    }

    @Test
    fun `rejects telemetry when question or choice is invalid`() {
        val service = service(CapturingBetaEventRepository())

        assertThrows(DiagnosticException.InvalidDiagnosticTelemetry::class.java) {
            service.record(
                RecordDiagnosticTelemetryCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    eventType = DiagnosticTelemetryEventType.QUESTION_SHOWN,
                    questionId = "missing-question",
                    idempotencyKey = "question-shown:missing",
                    occurredAt = Instant.parse("2026-05-27T05:59:00Z"),
                ),
            )
        }

        assertThrows(DiagnosticException.InvalidDiagnosticTelemetry::class.java) {
            service.record(
                RecordDiagnosticTelemetryCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    eventType = DiagnosticTelemetryEventType.ANSWER_SELECTED,
                    questionId = "function-substitution-1",
                    selectedChoiceId = "not-a-choice",
                    idempotencyKey = "answer-selected:invalid-choice",
                    occurredAt = Instant.parse("2026-05-27T05:59:00Z"),
                ),
            )
        }
    }

    private fun service(betaEventRepository: CapturingBetaEventRepository): RecordDiagnosticTelemetryService {
        return RecordDiagnosticTelemetryService(
            diagnosticSessionRepositoryPort = FakeTelemetryDiagnosticSessionRepository(diagnosticSession()),
            diagnosticSessionQuestionSnapshotRepositoryPort = FakeTelemetryQuestionSnapshotRepository(functionQuestions()),
            appendBetaEventService = appendService(betaEventRepository),
        )
    }

    private fun appendService(betaEventRepository: BetaEventRepositoryPort): AppendBetaEventService {
        return AppendBetaEventService(
            betaEventRepositoryPort = betaEventRepository,
            objectMapper = objectMapper,
            meterRegistry = SimpleMeterRegistry(),
            clock = clock,
            modeValue = "shadow",
        )
    }

    private fun diagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            status = DiagnosticSessionStatus.IN_PROGRESS,
            createdAt = Instant.parse("2026-05-27T05:00:00Z"),
            updatedAt = Instant.parse("2026-05-27T05:30:00Z"),
        )
    }

    private fun functionQuestions(): List<DiagnosticQuestion> {
        return listOf(
            DiagnosticQuestion(
                id = DiagnosticQuestionId("function-substitution-1"),
                mathArea = MathArea.FUNCTION,
                prompt = "함수 y = 2x + 1에서 x가 3일 때 y의 값은?",
                choices = choices("5", "7", "9"),
                correctChoiceId = DiagnosticQuestionChoiceId("b"),
                conceptTag = "function_substitution",
                displayOrder = 1,
            ),
            DiagnosticQuestion(
                id = DiagnosticQuestionId("function-slope-1"),
                mathArea = MathArea.FUNCTION,
                prompt = "일차함수 y = -3x + 2의 기울기는?",
                choices = choices("-3", "2", "3"),
                correctChoiceId = DiagnosticQuestionChoiceId("a"),
                conceptTag = "linear_function_slope",
                displayOrder = 2,
            ),
        )
    }

    private fun choices(a: String, b: String, c: String): List<DiagnosticQuestionChoice> {
        return listOf(
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("a"), a),
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("b"), b),
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("c"), c),
            DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("unknown"), "잘 모르겠음"),
        )
    }
}

private class CapturingBetaEventRepository : BetaEventRepositoryPort {
    val saved = mutableListOf<BetaEvent>()

    override fun save(event: BetaEvent): BetaEvent {
        saved += event
        return event
    }
}

private class FakeTelemetryDiagnosticSessionRepository(
    private val session: DiagnosticSession?,
) : DiagnosticSessionRepositoryPort {
    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return session?.takeIf { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        error("diagnostic session should not be updated")
    }
}

private class FakeTelemetryQuestionSnapshotRepository(
    private val questions: List<DiagnosticQuestion>,
) : DiagnosticSessionQuestionSnapshotRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): List<DiagnosticQuestion> {
        return questions.sortedBy { it.displayOrder }
    }

    override fun saveSnapshot(
        diagnosticSessionId: DiagnosticSessionId,
        questions: List<DiagnosticQuestion>,
    ): List<DiagnosticQuestion> {
        error("snapshot should not be saved")
    }
}

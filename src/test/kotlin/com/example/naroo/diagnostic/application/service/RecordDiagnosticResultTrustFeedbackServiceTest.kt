package com.example.naroo.diagnostic.application.service

import com.example.naroo.betaops.application.service.AppendBetaEventService
import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryOutcome
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticResultTrustFeedbackCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
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

class RecordDiagnosticResultTrustFeedbackServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T06:10:00Z"), ZoneOffset.UTC)
    private val objectMapper = ObjectMapper()

    @Test
    fun `records result trust feedback for completed diagnostic result`() {
        val betaEventRepository = TrustFeedbackCapturingBetaEventRepository()
        val service = service(
            session = completedDiagnosticSession(),
            result = diagnosticResult(),
            betaEventRepository = betaEventRepository,
        )

        val recorded = service.record(
            RecordDiagnosticResultTrustFeedbackCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
                feedbackChoice = DiagnosticResultTrustFeedbackChoice.FEELS_RIGHT,
                idempotencyKey = "trust-feedback:1",
                occurredAt = Instant.parse("2026-05-27T06:09:00Z"),
                flowVariant = " beta-v1 ",
                resultCopyVersion = " result-copy-v1 ",
            ),
        )

        assertEquals(DiagnosticTelemetryOutcome.APPENDED, recorded.outcome)
        val saved = betaEventRepository.saved.single()
        assertEquals(BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED, saved.eventType)
        assertEquals("diagnostic-session-1", saved.diagnosticSessionId)
        assertEquals("beta-v1", saved.flowVariant)
        assertEquals("result-copy-v1", saved.resultCopyVersion)
        assertEquals(3, saved.questionSnapshotVersion)
        assertTrue(saved.payloadJson.contains("\"feedbackChoice\":\"FEELS_RIGHT\""))
        assertTrue(saved.payloadJson.contains("\"primaryRecoveryConcept\":\"linear_function_slope\""))
    }

    @Test
    fun `rejects trust feedback when idempotency key is blank`() {
        val service = service(
            session = completedDiagnosticSession(),
            result = diagnosticResult(),
            betaEventRepository = TrustFeedbackCapturingBetaEventRepository(),
        )

        assertThrows(DiagnosticException.InvalidDiagnosticTrustFeedback::class.java) {
            service.record(
                RecordDiagnosticResultTrustFeedbackCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    feedbackChoice = DiagnosticResultTrustFeedbackChoice.UNSURE,
                    idempotencyKey = "  ",
                    occurredAt = Instant.parse("2026-05-27T06:09:00Z"),
                ),
            )
        }
    }

    @Test
    fun `rejects trust feedback before result is ready`() {
        val serviceWithIncompleteSession = service(
            session = completedDiagnosticSession().copy(status = DiagnosticSessionStatus.IN_PROGRESS),
            result = diagnosticResult(),
            betaEventRepository = TrustFeedbackCapturingBetaEventRepository(),
        )
        assertThrows(DiagnosticException.DiagnosticResultNotReady::class.java) {
            serviceWithIncompleteSession.record(
                RecordDiagnosticResultTrustFeedbackCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    feedbackChoice = DiagnosticResultTrustFeedbackChoice.UNSURE,
                    idempotencyKey = "trust-feedback:incomplete",
                    occurredAt = Instant.parse("2026-05-27T06:09:00Z"),
                ),
            )
        }

        val serviceWithMissingResult = service(
            session = completedDiagnosticSession(),
            result = null,
            betaEventRepository = TrustFeedbackCapturingBetaEventRepository(),
        )
        assertThrows(DiagnosticException.DiagnosticResultNotReady::class.java) {
            serviceWithMissingResult.record(
                RecordDiagnosticResultTrustFeedbackCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    feedbackChoice = DiagnosticResultTrustFeedbackChoice.UNSURE,
                    idempotencyKey = "trust-feedback:missing-result",
                    occurredAt = Instant.parse("2026-05-27T06:09:00Z"),
                ),
            )
        }
    }

    private fun service(
        session: DiagnosticSession?,
        result: DiagnosticResult?,
        betaEventRepository: BetaEventRepositoryPort,
    ): RecordDiagnosticResultTrustFeedbackService {
        return RecordDiagnosticResultTrustFeedbackService(
            diagnosticSessionRepositoryPort = TrustFeedbackDiagnosticSessionRepository(session),
            diagnosticResultRepositoryPort = TrustFeedbackDiagnosticResultRepository(result),
            appendBetaEventService = AppendBetaEventService(
                betaEventRepositoryPort = betaEventRepository,
                objectMapper = objectMapper,
                meterRegistry = SimpleMeterRegistry(),
                clock = clock,
                modeValue = "shadow",
            ),
        )
    }

    private fun completedDiagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            status = DiagnosticSessionStatus.COMPLETED,
            createdAt = Instant.parse("2026-05-27T05:00:00Z"),
            updatedAt = Instant.parse("2026-05-27T06:00:00Z"),
        )
    }

    private fun diagnosticResult(): DiagnosticResult {
        return DiagnosticResult(
            diagnosticSessionId = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            mathArea = MathArea.FUNCTION,
            totalQuestionCount = 2,
            correctCount = 1,
            wrongCount = 0,
            unknownCount = 1,
            weakLinks = listOf("linear_function_slope"),
            primaryRecoveryConcept = "linear_function_slope",
            summary = "전체가 무너진 게 아니에요.",
            createdAt = Instant.parse("2026-05-27T06:00:00Z"),
        )
    }
}

private class TrustFeedbackCapturingBetaEventRepository : BetaEventRepositoryPort {
    val saved = mutableListOf<BetaEvent>()

    override fun save(event: BetaEvent): BetaEvent {
        saved += event
        return event
    }
}

private class TrustFeedbackDiagnosticSessionRepository(
    private val session: DiagnosticSession?,
) : DiagnosticSessionRepositoryPort {
    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return session?.takeIf { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        error("session should not be saved")
    }
}

private class TrustFeedbackDiagnosticResultRepository(
    private val result: DiagnosticResult?,
) : DiagnosticResultRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return result?.takeIf { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findLatestByUserId(userId: UserId): DiagnosticResult? {
        return result?.takeIf { it.userId == userId }
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        error("result should not be saved")
    }
}

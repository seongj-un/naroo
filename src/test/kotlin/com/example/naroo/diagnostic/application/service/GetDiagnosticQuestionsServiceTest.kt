package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class GetDiagnosticQuestionsServiceTest {
    @Test
    fun `returns questions and moves ready session to in progress`() {
        val repository = CapturingQuestionDiagnosticSessionRepository(diagnosticSession())
        val service = GetDiagnosticQuestionsService(
            diagnosticSessionRepositoryPort = repository,
            clock = Clock.fixed(Instant.parse("2026-04-29T01:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.get(
            GetDiagnosticQuestionsCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals("diagnostic-session-1", result.diagnosticSessionId)
        assertEquals(MathArea.FUNCTION, result.mathArea)
        assertEquals(DiagnosticSessionStatus.IN_PROGRESS, result.status)
        assertEquals(DiagnosticSessionStatus.IN_PROGRESS, repository.saved.single().status)
        assertEquals(Instant.parse("2026-04-29T01:00:00Z"), repository.saved.single().updatedAt)
        assertEquals(2, result.questions.size)
        assertTrue(result.questions.all { question -> question.choices.any { it.id == "unknown" } })
    }

    @Test
    fun `does not rewrite already in progress session`() {
        val repository = CapturingQuestionDiagnosticSessionRepository(
            diagnosticSession().copy(status = DiagnosticSessionStatus.IN_PROGRESS),
        )
        val service = GetDiagnosticQuestionsService(
            diagnosticSessionRepositoryPort = repository,
            clock = Clock.fixed(Instant.parse("2026-04-29T01:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.get(
            GetDiagnosticQuestionsCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals(DiagnosticSessionStatus.IN_PROGRESS, result.status)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `rejects missing or other user's diagnostic session`() {
        val service = GetDiagnosticQuestionsService(
            diagnosticSessionRepositoryPort = CapturingQuestionDiagnosticSessionRepository(diagnosticSession()),
            clock = Clock.fixed(Instant.parse("2026-04-29T01:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(DiagnosticSessionNotFoundException::class.java) {
            service.get(
                GetDiagnosticQuestionsCommand(
                    userId = "other-user",
                    diagnosticSessionId = "diagnostic-session-1",
                ),
            )
        }
        assertThrows(DiagnosticSessionNotFoundException::class.java) {
            service.get(
                GetDiagnosticQuestionsCommand(
                    userId = "user-1",
                    diagnosticSessionId = "missing-session",
                ),
            )
        }
    }

    private fun diagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            status = DiagnosticSessionStatus.READY,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
            updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }
}

private class CapturingQuestionDiagnosticSessionRepository(
    private var session: DiagnosticSession?,
) : DiagnosticSessionRepositoryPort {
    val saved = mutableListOf<DiagnosticSession>()

    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return session?.takeIf { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        this.session = session
        saved += session
        return session
    }
}

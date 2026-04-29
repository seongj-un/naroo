package com.example.naroo.diagnostic.application.service

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
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
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
            diagnosticQuestionRepositoryPort = FakeDiagnosticQuestionRepository(functionQuestions()),
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
            diagnosticQuestionRepositoryPort = FakeDiagnosticQuestionRepository(functionQuestions()),
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
            diagnosticQuestionRepositoryPort = FakeDiagnosticQuestionRepository(functionQuestions()),
            clock = Clock.fixed(Instant.parse("2026-04-29T01:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(DiagnosticException.DiagnosticSessionNotFound::class.java) {
            service.get(
                GetDiagnosticQuestionsCommand(
                    userId = "other-user",
                    diagnosticSessionId = "diagnostic-session-1",
                ),
            )
        }
        assertThrows(DiagnosticException.DiagnosticSessionNotFound::class.java) {
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

private class FakeDiagnosticQuestionRepository(
    private val questions: List<DiagnosticQuestion>,
) : DiagnosticQuestionRepositoryPort {
    override fun findByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.filter { it.mathArea == mathArea }.sortedBy { it.displayOrder }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return questions.sortedWith(compareBy<DiagnosticQuestion> { it.mathArea }.thenBy { it.displayOrder })
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

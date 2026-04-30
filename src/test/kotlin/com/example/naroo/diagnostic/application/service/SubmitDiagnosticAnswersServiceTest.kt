package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticAnswer
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswerCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticAnswerRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SubmitDiagnosticAnswersServiceTest {
    @Test
    fun `stores answers, completes session, and returns scored result`() {
        val sessionRepository = CapturingSubmitDiagnosticSessionRepository(diagnosticSession())
        val answerRepository = CapturingDiagnosticAnswerRepository()
        val resultRepository = CapturingDiagnosticResultRepository()
        val service = SubmitDiagnosticAnswersService(
            diagnosticSessionRepositoryPort = sessionRepository,
            diagnosticAnswerRepositoryPort = answerRepository,
            diagnosticQuestionRepositoryPort = FakeSubmitDiagnosticQuestionRepository(functionQuestions()),
            diagnosticResultRepositoryPort = resultRepository,
            clock = Clock.fixed(Instant.parse("2026-04-29T02:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.submit(
            SubmitDiagnosticAnswersCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
                answers = listOf(
                    SubmitDiagnosticAnswerCommand("function-substitution-1", "b"),
                    SubmitDiagnosticAnswerCommand("function-slope-1", "unknown"),
                ),
            ),
        )

        assertEquals(DiagnosticSessionStatus.COMPLETED, result.status)
        assertEquals(2, result.totalQuestionCount)
        assertEquals(1, result.correctCount)
        assertEquals(0, result.wrongCount)
        assertEquals(1, result.unknownCount)
        assertEquals(listOf("linear_function_slope"), result.weakLinks)
        assertEquals("linear_function_slope", result.primaryRecoveryConcept)
        assertEquals(2, answerRepository.saved.size)
        assertEquals(true, answerRepository.saved.first { it.questionId.value == "function-substitution-1" }.isCorrect)
        assertEquals(true, answerRepository.saved.first { it.questionId.value == "function-slope-1" }.isUnknown)
        assertEquals(DiagnosticSessionStatus.COMPLETED, sessionRepository.saved.single().status)
        assertEquals("linear_function_slope", resultRepository.saved.single().primaryRecoveryConcept)
    }

    @Test
    fun `rejects duplicate submission`() {
        val service = SubmitDiagnosticAnswersService(
            diagnosticSessionRepositoryPort = CapturingSubmitDiagnosticSessionRepository(
                diagnosticSession().copy(status = DiagnosticSessionStatus.COMPLETED),
            ),
            diagnosticAnswerRepositoryPort = CapturingDiagnosticAnswerRepository(),
            diagnosticQuestionRepositoryPort = FakeSubmitDiagnosticQuestionRepository(functionQuestions()),
            diagnosticResultRepositoryPort = CapturingDiagnosticResultRepository(),
            clock = Clock.fixed(Instant.parse("2026-04-29T02:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(DiagnosticException.DiagnosticAlreadyCompleted::class.java) {
            service.submit(validCommand())
        }
    }

    @Test
    fun `rejects missing answer`() {
        val service = SubmitDiagnosticAnswersService(
            diagnosticSessionRepositoryPort = CapturingSubmitDiagnosticSessionRepository(diagnosticSession()),
            diagnosticAnswerRepositoryPort = CapturingDiagnosticAnswerRepository(),
            diagnosticQuestionRepositoryPort = FakeSubmitDiagnosticQuestionRepository(functionQuestions()),
            diagnosticResultRepositoryPort = CapturingDiagnosticResultRepository(),
            clock = Clock.fixed(Instant.parse("2026-04-29T02:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(DiagnosticException.InvalidDiagnosticAnswer::class.java) {
            service.submit(
                SubmitDiagnosticAnswersCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                    answers = listOf(
                        SubmitDiagnosticAnswerCommand("function-substitution-1", "b"),
                    ),
                ),
            )
        }
    }

    private fun validCommand(): SubmitDiagnosticAnswersCommand {
        return SubmitDiagnosticAnswersCommand(
            userId = "user-1",
            diagnosticSessionId = "diagnostic-session-1",
            answers = listOf(
                SubmitDiagnosticAnswerCommand("function-substitution-1", "b"),
                SubmitDiagnosticAnswerCommand("function-slope-1", "unknown"),
            ),
        )
    }

    private fun diagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            status = DiagnosticSessionStatus.IN_PROGRESS,
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

private class FakeSubmitDiagnosticQuestionRepository(
    private val questions: List<DiagnosticQuestion>,
) : DiagnosticQuestionRepositoryPort {
    override fun findActiveByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.filter { it.mathArea == mathArea }.sortedBy { it.displayOrder }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return questions.sortedWith(compareBy<DiagnosticQuestion> { it.mathArea }.thenBy { it.displayOrder })
    }
}

private class CapturingSubmitDiagnosticSessionRepository(
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

private class CapturingDiagnosticAnswerRepository : DiagnosticAnswerRepositoryPort {
    val saved = mutableListOf<DiagnosticAnswer>()

    override fun existsByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): Boolean {
        return saved.any { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun saveAll(answers: List<DiagnosticAnswer>): List<DiagnosticAnswer> {
        saved += answers
        return answers
    }
}

private class CapturingDiagnosticResultRepository : DiagnosticResultRepositoryPort {
    val saved = mutableListOf<DiagnosticResult>()

    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return saved.firstOrNull { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findLatestByUserId(userId: UserId): DiagnosticResult? {
        return saved.filter { it.userId == userId }.maxByOrNull { it.createdAt }
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        saved += result
        return result
    }
}

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
import com.example.naroo.diagnostic.domain.StartingPointNote
import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionIdGeneratorPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CreateDiagnosticSessionServiceTest {
    @Test
    fun `creates diagnostic session from selected starting point`() {
        val sessionRepository = CapturingDiagnosticSessionRepository()
        val service = CreateDiagnosticSessionService(
            startingPointSelectionRepositoryPort = FakeSessionStartingPointRepository(startingPointSelection()),
            diagnosticSessionRepositoryPort = sessionRepository,
            diagnosticQuestionRepositoryPort = FakeSessionDiagnosticQuestionRepository(functionQuestions()),
            diagnosticSessionQuestionSnapshotRepositoryPort = CapturingSessionQuestionSnapshotRepository(),
            diagnosticSessionIdGeneratorPort = DiagnosticSessionIdGeneratorPort {
                DiagnosticSessionId("diagnostic-session-1")
            },
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.create(CreateDiagnosticSessionCommand(userId = "user-1"))

        assertEquals("diagnostic-session-1", result.id)
        assertEquals("user-1", result.userId)
        assertEquals("starting-point-1", result.startingPointSelectionId)
        assertEquals(MathArea.FUNCTION, result.mathArea)
        assertEquals(1, result.questionSnapshotVersion)
        assertEquals(DiagnosticSessionStatus.READY, result.status)
        assertEquals(Instant.parse("2026-04-29T00:00:00Z"), result.createdAt)
        assertEquals(sessionRepository.saved.single().id.value, result.id)
    }

    @Test
    fun `rejects diagnostic session creation without starting point`() {
        val service = CreateDiagnosticSessionService(
            startingPointSelectionRepositoryPort = FakeSessionStartingPointRepository(null),
            diagnosticSessionRepositoryPort = CapturingDiagnosticSessionRepository(),
            diagnosticQuestionRepositoryPort = FakeSessionDiagnosticQuestionRepository(functionQuestions()),
            diagnosticSessionQuestionSnapshotRepositoryPort = CapturingSessionQuestionSnapshotRepository(),
            diagnosticSessionIdGeneratorPort = DiagnosticSessionIdGeneratorPort {
                DiagnosticSessionId("diagnostic-session-1")
            },
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(DiagnosticException.StartingPointSelectionRequired::class.java) {
            service.create(CreateDiagnosticSessionCommand(userId = "user-1"))
        }
    }

    private fun startingPointSelection(): StartingPointSelection {
        return StartingPointSelection(
            id = StartingPointSelectionId("starting-point-1"),
            userId = UserId("user-1"),
            selectionType = StartingPointSelectionType.WEAK_AREA,
            mathArea = MathArea.FUNCTION,
            note = StartingPointNote.fromNullable("함수가 어려워요"),
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
            updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }

    private fun functionQuestions(): List<DiagnosticQuestion> {
        return listOf(
            DiagnosticQuestion(
                id = DiagnosticQuestionId("function-slope-1"),
                mathArea = MathArea.FUNCTION,
                prompt = "일차함수 y = -3x + 2의 기울기는?",
                choices = listOf(
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("a"), "-3"),
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("unknown"), "잘 모르겠음"),
                ),
                correctChoiceId = DiagnosticQuestionChoiceId("a"),
                conceptTag = "linear_function_slope",
                displayOrder = 1,
            ),
        )
    }
}

private class FakeSessionDiagnosticQuestionRepository(
    private val questions: List<DiagnosticQuestion>,
) : DiagnosticQuestionRepositoryPort {
    override fun findActiveByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.filter { it.mathArea == mathArea }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return questions
    }

    override fun save(question: DiagnosticQuestion): DiagnosticQuestion {
        error("question should not be saved")
    }
}

private class CapturingSessionQuestionSnapshotRepository : DiagnosticSessionQuestionSnapshotRepositoryPort {
    val saved = mutableMapOf<DiagnosticSessionId, List<DiagnosticQuestion>>()

    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): List<DiagnosticQuestion> {
        return saved[diagnosticSessionId].orEmpty()
    }

    override fun saveSnapshot(
        diagnosticSessionId: DiagnosticSessionId,
        questions: List<DiagnosticQuestion>,
    ): List<DiagnosticQuestion> {
        saved[diagnosticSessionId] = questions
        return questions
    }
}

private class FakeSessionStartingPointRepository(
    private val selection: StartingPointSelection?,
) : StartingPointSelectionRepositoryPort {
    override fun findByUserId(userId: UserId): StartingPointSelection? {
        return selection?.takeIf { it.userId == userId }
    }

    override fun save(selection: StartingPointSelection): StartingPointSelection {
        error("starting point should not be saved")
    }
}

private class CapturingDiagnosticSessionRepository : DiagnosticSessionRepositoryPort {
    val saved = mutableListOf<DiagnosticSession>()

    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return saved.firstOrNull { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        saved += session
        return session
    }
}

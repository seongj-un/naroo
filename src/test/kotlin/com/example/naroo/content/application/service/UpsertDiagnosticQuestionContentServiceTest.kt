package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionChoiceContentCommand
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionContentCommand
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UpsertDiagnosticQuestionContentServiceTest {
    @Test
    fun `upserts diagnostic question content`() {
        val repository = CapturingContentDiagnosticQuestionRepository()
        val service = UpsertDiagnosticQuestionContentService(repository)

        val result = service.upsert(
            UpsertDiagnosticQuestionContentCommand(
                id = "function-slope-2",
                mathArea = MathArea.FUNCTION,
                prompt = "기울기는?",
                correctChoiceId = "a",
                conceptTag = "linear_function_slope",
                displayOrder = 3,
                status = "ACTIVE",
                choices = listOf(
                    UpsertDiagnosticQuestionChoiceContentCommand("a", "-3"),
                    UpsertDiagnosticQuestionChoiceContentCommand("unknown", "잘 모르겠음"),
                ),
            ),
        )

        assertEquals("function-slope-2", result.id)
        assertEquals("a", result.correctChoiceId)
        assertEquals("ACTIVE", result.status)
        assertEquals(2, repository.saved.single().choices.size)
    }

    @Test
    fun `rejects missing correct choice`() {
        val service = UpsertDiagnosticQuestionContentService(CapturingContentDiagnosticQuestionRepository())

        assertThrows(IllegalArgumentException::class.java) {
            service.upsert(
                UpsertDiagnosticQuestionContentCommand(
                    id = "function-slope-2",
                    mathArea = MathArea.FUNCTION,
                    prompt = "기울기는?",
                    correctChoiceId = "missing",
                    conceptTag = "linear_function_slope",
                    displayOrder = 3,
                    status = "ACTIVE",
                    choices = listOf(UpsertDiagnosticQuestionChoiceContentCommand("a", "-3")),
                ),
            )
        }
    }
}

private class CapturingContentDiagnosticQuestionRepository : DiagnosticQuestionRepositoryPort {
    val saved = mutableListOf<DiagnosticQuestion>()

    override fun findActiveByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return saved.filter { it.mathArea == mathArea }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return saved
    }

    override fun save(question: DiagnosticQuestion): DiagnosticQuestion {
        saved += question
        return question
    }
}

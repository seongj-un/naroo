package com.example.naroo.content.application.service

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListDiagnosticQuestionContentsServiceTest {
    @Test
    fun `lists diagnostic questions with correct choice for content QA`() {
        val service = ListDiagnosticQuestionContentsService(
            diagnosticQuestionRepositoryPort = FakeContentDiagnosticQuestionRepository(
                questions = listOf(functionQuestion()),
            ),
        )

        val result = service.list()

        assertEquals("function-slope-1", result.single().id)
        assertEquals("a", result.single().correctChoiceId)
        assertEquals("linear_function_slope", result.single().conceptTag)
        assertEquals(2, result.single().choices.size)
    }

    private fun functionQuestion(): DiagnosticQuestion {
        return DiagnosticQuestion(
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
        )
    }
}

private class FakeContentDiagnosticQuestionRepository(
    private val questions: List<DiagnosticQuestion>,
) : DiagnosticQuestionRepositoryPort {
    override fun findActiveByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.filter { it.mathArea == mathArea }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return questions
    }
}

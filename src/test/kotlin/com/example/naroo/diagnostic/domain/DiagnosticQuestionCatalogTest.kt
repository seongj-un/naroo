package com.example.naroo.diagnostic.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagnosticQuestionCatalogTest {
    @Test
    fun `provides two questions with unknown choice for every math area`() {
        MathArea.entries.forEach { mathArea ->
            val questions = DiagnosticQuestionCatalog.findByMathArea(mathArea)

            assertEquals(2, questions.size, "$mathArea should have two questions")
            assertEquals(listOf(1, 2), questions.map { it.displayOrder })
            assertTrue(questions.all { it.prompt.isNotBlank() })
            assertTrue(questions.all { it.choices.map { choice -> choice.id.value }.contains("unknown") })
        }
    }
}

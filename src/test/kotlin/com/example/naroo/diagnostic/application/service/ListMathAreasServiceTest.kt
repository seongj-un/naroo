package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.domain.MathArea
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListMathAreasServiceTest {
    @Test
    fun `lists math areas in display order`() {
        val result = ListMathAreasService().list()

        assertEquals(
            listOf(
                MathArea.EQUATION,
                MathArea.FUNCTION,
                MathArea.GEOMETRY,
                MathArea.PROBABILITY_AND_STATISTICS,
                MathArea.SEQUENCE,
            ),
            result.map { it.code },
        )
        assertEquals(listOf(1, 2, 3, 4, 5), result.map { it.displayOrder })
        assertTrue(result.all { it.name.isNotBlank() })
        assertTrue(result.all { it.description.isNotBlank() })
        assertTrue(result.all { it.recommendedFor.isNotBlank() })
    }
}

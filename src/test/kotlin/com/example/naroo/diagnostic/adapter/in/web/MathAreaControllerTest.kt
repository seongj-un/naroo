package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`in`.ListMathAreasUseCase
import com.example.naroo.diagnostic.port.`in`.MathAreaResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MathAreaControllerTest {
    @Test
    fun `list returns math area responses`() {
        val controller = MathAreaController(
            ListMathAreasUseCase {
                listOf(
                    MathAreaResult(
                        code = MathArea.FUNCTION,
                        name = "함수",
                        description = "그래프와 식이 헷갈리는 경우",
                        recommendedFor = "고등 수학 초반에서 막히는 학생",
                        displayOrder = 1,
                    ),
                )
            },
        )

        val response = controller.list()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(MathArea.FUNCTION, response.body?.data?.single()?.code)
        assertEquals("함수", response.body?.data?.single()?.name)
        assertEquals(1, response.body?.data?.single()?.displayOrder)
    }
}

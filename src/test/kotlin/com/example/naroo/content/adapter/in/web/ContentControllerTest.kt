package com.example.naroo.content.adapter.`in`.web

import com.example.naroo.content.port.`in`.DiagnosticQuestionChoiceContentResult
import com.example.naroo.content.port.`in`.DiagnosticQuestionContentResult
import com.example.naroo.content.port.`in`.ListDiagnosticQuestionContentsUseCase
import com.example.naroo.content.port.`in`.ListRecoveryMissionTemplateContentsUseCase
import com.example.naroo.content.port.`in`.RecoveryMissionTemplateContentResult
import com.example.naroo.diagnostic.domain.MathArea
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContentControllerTest {
    @Test
    fun `lists diagnostic question contents`() {
        val controller = ContentController(
            listDiagnosticQuestionContentsUseCase = ListDiagnosticQuestionContentsUseCase {
                listOf(
                    DiagnosticQuestionContentResult(
                        id = "function-slope-1",
                        mathArea = MathArea.FUNCTION,
                        prompt = "일차함수 y = -3x + 2의 기울기는?",
                        correctChoiceId = "a",
                        conceptTag = "linear_function_slope",
                        displayOrder = 1,
                        choices = listOf(DiagnosticQuestionChoiceContentResult("a", "-3")),
                    ),
                )
            },
            listRecoveryMissionTemplateContentsUseCase = ListRecoveryMissionTemplateContentsUseCase {
                error("templates should not be listed")
            },
        )

        val response = controller.listDiagnosticQuestions()

        assertEquals("function-slope-1", response.data?.questions?.single()?.id)
        assertEquals("a", response.data?.questions?.single()?.correctChoiceId)
    }

    @Test
    fun `lists recovery mission template contents`() {
        val controller = ContentController(
            listDiagnosticQuestionContentsUseCase = ListDiagnosticQuestionContentsUseCase {
                error("questions should not be listed")
            },
            listRecoveryMissionTemplateContentsUseCase = ListRecoveryMissionTemplateContentsUseCase {
                listOf(
                    RecoveryMissionTemplateContentResult(
                        conceptTag = "linear_function_slope",
                        title = "일차함수 기울기 10분 복구 미션",
                        prompt = "기울기를 찾는 연습만 해요.",
                        hints = listOf("x 앞에 붙은 숫자를 먼저 찾아봐요."),
                        estimatedMinutes = 10,
                    ),
                )
            },
        )

        val response = controller.listRecoveryMissionTemplates()

        assertEquals("linear_function_slope", response.data?.templates?.single()?.conceptTag)
        assertEquals(1, response.data?.templates?.single()?.hints?.size)
    }
}

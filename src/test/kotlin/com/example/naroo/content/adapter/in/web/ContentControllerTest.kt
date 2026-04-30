package com.example.naroo.content.adapter.`in`.web

import com.example.naroo.content.port.`in`.DiagnosticQuestionChoiceContentResult
import com.example.naroo.content.port.`in`.DiagnosticQuestionContentResult
import com.example.naroo.content.port.`in`.ListDiagnosticQuestionContentsUseCase
import com.example.naroo.content.port.`in`.ListRecoveryMissionTemplateContentsUseCase
import com.example.naroo.content.port.`in`.RecoveryMissionTemplateContentResult
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionContentCommand
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionContentUseCase
import com.example.naroo.content.port.`in`.UpsertRecoveryMissionTemplateContentCommand
import com.example.naroo.content.port.`in`.UpsertRecoveryMissionTemplateContentUseCase
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
                        status = "ACTIVE",
                        choices = listOf(DiagnosticQuestionChoiceContentResult("a", "-3")),
                    ),
                )
            },
            listRecoveryMissionTemplateContentsUseCase = ListRecoveryMissionTemplateContentsUseCase {
                error("templates should not be listed")
            },
            upsertDiagnosticQuestionContentUseCase = UpsertDiagnosticQuestionContentUseCase {
                error("question should not be upserted")
            },
            upsertRecoveryMissionTemplateContentUseCase = UpsertRecoveryMissionTemplateContentUseCase {
                error("template should not be upserted")
            },
            contentAdminAuthorizer = ContentAdminAuthorizer("admin-token"),
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
                        status = "ACTIVE",
                    ),
                )
            },
            upsertDiagnosticQuestionContentUseCase = UpsertDiagnosticQuestionContentUseCase {
                error("question should not be upserted")
            },
            upsertRecoveryMissionTemplateContentUseCase = UpsertRecoveryMissionTemplateContentUseCase {
                error("template should not be upserted")
            },
            contentAdminAuthorizer = ContentAdminAuthorizer("admin-token"),
        )

        val response = controller.listRecoveryMissionTemplates()

        assertEquals("linear_function_slope", response.data?.templates?.single()?.conceptTag)
        assertEquals(1, response.data?.templates?.single()?.hints?.size)
    }

    @Test
    fun `upserts diagnostic question with admin token`() {
        var capturedCommand: UpsertDiagnosticQuestionContentCommand? = null
        val controller = ContentController(
            listDiagnosticQuestionContentsUseCase = ListDiagnosticQuestionContentsUseCase {
                error("questions should not be listed")
            },
            listRecoveryMissionTemplateContentsUseCase = ListRecoveryMissionTemplateContentsUseCase {
                error("templates should not be listed")
            },
            upsertDiagnosticQuestionContentUseCase = UpsertDiagnosticQuestionContentUseCase { command ->
                capturedCommand = command
                DiagnosticQuestionContentResult(
                    id = command.id,
                    mathArea = command.mathArea,
                    prompt = command.prompt,
                    correctChoiceId = command.correctChoiceId,
                    conceptTag = command.conceptTag,
                    displayOrder = command.displayOrder,
                    status = command.status,
                    choices = command.choices.map { DiagnosticQuestionChoiceContentResult(it.id, it.text) },
                )
            },
            upsertRecoveryMissionTemplateContentUseCase = UpsertRecoveryMissionTemplateContentUseCase {
                error("template should not be upserted")
            },
            contentAdminAuthorizer = ContentAdminAuthorizer("admin-token"),
        )

        val response = controller.upsertDiagnosticQuestion(
            questionId = "function-slope-2",
            adminToken = "admin-token",
            request = UpsertDiagnosticQuestionContentRequest(
                mathArea = MathArea.FUNCTION,
                prompt = "기울기는?",
                correctChoiceId = "a",
                conceptTag = "linear_function_slope",
                displayOrder = 3,
                choices = listOf(UpsertDiagnosticQuestionChoiceContentRequest("a", "-3")),
            ),
        )

        assertEquals("function-slope-2", capturedCommand?.id)
        assertEquals("function-slope-2", response.data?.id)
    }

    @Test
    fun `upserts recovery mission template with admin token`() {
        var capturedCommand: UpsertRecoveryMissionTemplateContentCommand? = null
        val controller = ContentController(
            listDiagnosticQuestionContentsUseCase = ListDiagnosticQuestionContentsUseCase {
                error("questions should not be listed")
            },
            listRecoveryMissionTemplateContentsUseCase = ListRecoveryMissionTemplateContentsUseCase {
                error("templates should not be listed")
            },
            upsertDiagnosticQuestionContentUseCase = UpsertDiagnosticQuestionContentUseCase {
                error("question should not be upserted")
            },
            upsertRecoveryMissionTemplateContentUseCase = UpsertRecoveryMissionTemplateContentUseCase { command ->
                capturedCommand = command
                RecoveryMissionTemplateContentResult(
                    conceptTag = command.conceptTag,
                    title = command.title,
                    prompt = command.prompt,
                    hints = command.hints,
                    estimatedMinutes = command.estimatedMinutes,
                    status = command.status,
                )
            },
            contentAdminAuthorizer = ContentAdminAuthorizer("admin-token"),
        )

        val response = controller.upsertRecoveryMissionTemplate(
            conceptTag = "linear_function_slope",
            adminToken = "admin-token",
            request = UpsertRecoveryMissionTemplateContentRequest(
                title = "기울기 복구",
                prompt = "기울기만 찾아요.",
                hints = listOf("x 앞 숫자를 봐요."),
                estimatedMinutes = 8,
            ),
        )

        assertEquals("linear_function_slope", capturedCommand?.conceptTag)
        assertEquals("linear_function_slope", response.data?.conceptTag)
    }
}

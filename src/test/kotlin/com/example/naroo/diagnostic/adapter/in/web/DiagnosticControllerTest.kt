package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionUseCase
import com.example.naroo.diagnostic.port.`in`.CreatedDiagnosticSessionResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionChoiceResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionsResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultView
import com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryEventType
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultUseCase
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsUseCase
import com.example.naroo.diagnostic.port.`in`.NextMissionPreviewResult
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticTelemetryCommand
import com.example.naroo.diagnostic.port.`in`.RecordDiagnosticTelemetryUseCase
import com.example.naroo.diagnostic.port.`in`.RecordedDiagnosticTelemetryResult
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import com.example.naroo.diagnostic.port.`in`.SelectedStartingPointResult
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswerCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersUseCase
import com.example.naroo.diagnostic.port.`in`.SubmittedDiagnosticResult
import com.example.naroo.support.noOpBusinessStageBetaEventTracker
import com.example.naroo.support.noOpRecordDiagnosticTelemetryUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

class DiagnosticControllerTest {
    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `get questions returns diagnostic questions for verified user`() {
        var capturedCommand: GetDiagnosticQuestionsCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { command ->
                capturedCommand = command
                DiagnosticQuestionsResult(
                    diagnosticSessionId = command.diagnosticSessionId,
                    mathArea = MathArea.FUNCTION,
                    status = DiagnosticSessionStatus.IN_PROGRESS,
                    questions = listOf(
                        DiagnosticQuestionResult(
                            id = "function-substitution-1",
                            prompt = "함수 y = 2x + 1에서 x가 3일 때 y의 값은?",
                            choices = listOf(
                                DiagnosticQuestionChoiceResult(id = "a", text = "5"),
                                DiagnosticQuestionChoiceResult(id = "b", text = "7"),
                                DiagnosticQuestionChoiceResult(id = "unknown", text = "잘 모르겠음"),
                            ),
                        ),
                    ),
                )
            },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )

        authenticate(emailVerified = true)
        val response = controller.getQuestions(diagnosticSessionId = "diagnostic-session-1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", capturedCommand?.diagnosticSessionId)
        assertEquals(MathArea.FUNCTION, response.body?.data?.mathArea)
        assertEquals(DiagnosticSessionStatus.IN_PROGRESS, response.body?.data?.status)
        assertEquals("function-substitution-1", response.body?.data?.questions?.single()?.id)
        assertEquals("unknown", response.body?.data?.questions?.single()?.choices?.last()?.id)
    }

    @Test
    fun `get questions requires verified email`() {
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )

        authenticate(emailVerified = false)
        assertThrows(DiagnosticException.EmailVerificationRequired::class.java) {
            controller.getQuestions(diagnosticSessionId = "diagnostic-session-1")
        }
    }

    @Test
    fun `create diagnostic session returns created response for verified user`() {
        var capturedCommand: CreateDiagnosticSessionCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { command ->
                capturedCommand = command
                CreatedDiagnosticSessionResult(
                    id = "diagnostic-session-1",
                    userId = command.userId,
                    startingPointSelectionId = "starting-point-1",
                    mathArea = MathArea.FUNCTION,
                    questionSnapshotVersion = 1,
                    status = DiagnosticSessionStatus.READY,
                    createdAt = Instant.parse("2026-04-29T00:00:00Z"),
                    updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
                )
            },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )

        authenticate(emailVerified = true)
        val response = controller.createDiagnosticSession()

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", response.body?.data?.id)
        assertEquals("starting-point-1", response.body?.data?.startingPointSelectionId)
        assertEquals(MathArea.FUNCTION, response.body?.data?.mathArea)
        assertEquals(DiagnosticSessionStatus.READY, response.body?.data?.status)
    }

    @Test
    fun `create diagnostic session requires verified email`() {
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )

        authenticate(emailVerified = false)
        assertThrows(DiagnosticException.EmailVerificationRequired::class.java) {
            controller.createDiagnosticSession()
        }
    }

    @Test
    fun `select starting point returns created response for verified user`() {
        var capturedCommand: SelectStartingPointCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { command ->
                capturedCommand = command
                SelectedStartingPointResult(
                    id = "starting-point-1",
                    userId = command.userId,
                    selectionType = command.selectionType,
                    mathArea = command.mathArea,
                    note = command.note?.trim(),
                    createdAt = Instant.parse("2026-04-29T00:00:00Z"),
                    updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
                )
            },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.selectStartingPoint(
            request = SelectStartingPointRequest(
                selectionType = StartingPointSelectionType.STUDY_INTEREST,
                mathArea = MathArea.SEQUENCE,
                note = "수열을 다시 해보고 싶어요",
            ),
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals(StartingPointSelectionType.STUDY_INTEREST, capturedCommand?.selectionType)
        assertEquals(MathArea.SEQUENCE, capturedCommand?.mathArea)
        assertEquals("수열을 다시 해보고 싶어요", response.body?.data?.note)
    }

    @Test
    fun `select starting point requires verified email`() {
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("use case should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )

        authenticate(emailVerified = false)
        assertThrows(DiagnosticException.EmailVerificationRequired::class.java) {
            controller.selectStartingPoint(
                request = SelectStartingPointRequest(
                    selectionType = StartingPointSelectionType.WEAK_AREA,
                    mathArea = MathArea.FUNCTION,
                    note = null,
                ),
            )
        }
    }

    @Test
    fun `submit answers returns completed diagnostic result for verified user`() {
        var capturedCommand: SubmitDiagnosticAnswersCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { command ->
                capturedCommand = command
                SubmittedDiagnosticResult(
                    diagnosticSessionId = command.diagnosticSessionId,
                    mathArea = MathArea.FUNCTION,
                    status = DiagnosticSessionStatus.COMPLETED,
                    totalQuestionCount = 2,
                    correctCount = 1,
                    wrongCount = 0,
                    unknownCount = 1,
                    weakLinks = listOf("linear_function_slope"),
                    primaryRecoveryConcept = "linear_function_slope",
                    summary = "전체가 무너진 게 아니에요.",
                )
            },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.submitAnswers(
            diagnosticSessionId = "diagnostic-session-1",
            request = SubmitDiagnosticAnswersRequest(
                answers = listOf(
                    SubmitDiagnosticAnswerRequest(
                        questionId = "function-substitution-1",
                        selectedChoiceId = "b",
                    ),
                    SubmitDiagnosticAnswerRequest(
                        questionId = "function-slope-1",
                        selectedChoiceId = "unknown",
                    ),
                ),
            ),
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", capturedCommand?.diagnosticSessionId)
        assertEquals(
            listOf(
                SubmitDiagnosticAnswerCommand("function-substitution-1", "b"),
                SubmitDiagnosticAnswerCommand("function-slope-1", "unknown"),
            ),
            capturedCommand?.answers,
        )
        assertEquals(DiagnosticSessionStatus.COMPLETED, response.body?.data?.status)
        assertEquals(1, response.body?.data?.correctCount)
        assertEquals(1, response.body?.data?.unknownCount)
        assertEquals("linear_function_slope", response.body?.data?.primaryRecoveryConcept)
    }

    @Test
    fun `record telemetry returns accepted response for verified user`() {
        var capturedCommand: RecordDiagnosticTelemetryCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { error("get result should not be called") },
            recordDiagnosticTelemetryUseCase = RecordDiagnosticTelemetryUseCase { command ->
                capturedCommand = command
                RecordedDiagnosticTelemetryResult(
                    diagnosticSessionId = command.diagnosticSessionId,
                    eventType = command.eventType,
                    outcome = com.example.naroo.diagnostic.port.`in`.DiagnosticTelemetryOutcome.APPENDED,
                )
            },
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.recordTelemetry(
            diagnosticSessionId = "diagnostic-session-1",
            request = RecordDiagnosticTelemetryRequest(
                eventType = DiagnosticTelemetryEventType.QUESTION_SHOWN,
                questionId = "function-substitution-1",
                idempotencyKey = "question-shown:1",
                occurredAt = Instant.parse("2026-05-27T06:00:00Z"),
                flowVariant = "beta-v1",
            ),
        )

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", capturedCommand?.diagnosticSessionId)
        assertEquals(DiagnosticTelemetryEventType.QUESTION_SHOWN, capturedCommand?.eventType)
        assertEquals("function-substitution-1", capturedCommand?.questionId)
        assertEquals("question-shown:1", capturedCommand?.idempotencyKey)
        assertEquals("beta-v1", capturedCommand?.flowVariant)
        assertEquals("APPENDED", response.body?.data?.outcome?.name)
    }

    @Test
    fun `get result returns diagnostic result with next mission preview`() {
        var capturedCommand: GetDiagnosticResultCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("select starting point should not be called") },
            CreateDiagnosticSessionUseCase { error("create diagnostic session should not be called") },
            GetDiagnosticQuestionsUseCase { error("get questions should not be called") },
            SubmitDiagnosticAnswersUseCase { error("submit answers should not be called") },
            GetDiagnosticResultUseCase { command ->
                capturedCommand = command
                DiagnosticResultView(
                    diagnosticSessionId = command.diagnosticSessionId,
                    mathArea = MathArea.FUNCTION,
                    status = DiagnosticSessionStatus.COMPLETED,
                    totalQuestionCount = 2,
                    correctCount = 1,
                    wrongCount = 0,
                    unknownCount = 1,
                    weakLinks = listOf("linear_function_slope"),
                    primaryRecoveryConcept = "linear_function_slope",
                    summary = "전체가 무너진 게 아니에요.",
                    nextMissionPreview = NextMissionPreviewResult(
                        conceptTag = "linear_function_slope",
                        title = "linear_function_slope 10분 복구 미션",
                        estimatedMinutes = 10,
                        tone = "힌트부터 천천히 시작해요.",
                    ),
                )
            },
            recordDiagnosticTelemetryUseCase = noOpRecordDiagnosticTelemetryUseCase(),
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.getResult(diagnosticSessionId = "diagnostic-session-1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", capturedCommand?.diagnosticSessionId)
        assertEquals(DiagnosticSessionStatus.COMPLETED, response.body?.data?.status)
        assertEquals("linear_function_slope", response.body?.data?.primaryRecoveryConcept)
        assertEquals(10, response.body?.data?.nextMissionPreview?.estimatedMinutes)
    }

    private fun authenticate(emailVerified: Boolean) {
        val authentication = JwtAuthentication(
            tokenId = "token-1",
            userId = "user-1",
            loginId = "student01",
            emailVerified = emailVerified,
            nickname = "나루",
            role = "STUDENT",
        )
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(authentication, null, emptyList())
    }
}

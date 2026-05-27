package com.example.naroo.recovery.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import com.example.naroo.recovery.port.`in`.RecoveryMissionSubmissionResult
import com.example.naroo.recovery.port.`in`.SubmitRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.SubmitRecoveryMissionUseCase
import com.example.naroo.support.noOpBusinessStageBetaEventTracker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

class RecoveryMissionControllerTest {
    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `create starts first recovery mission for verified user`() {
        var capturedCommand: CreateRecoveryMissionCommand? = null
        val controller = RecoveryMissionController(
            createRecoveryMissionUseCase = CreateRecoveryMissionUseCase { command ->
                capturedCommand = command
                recoveryMissionResult()
            },
            getRecoveryMissionUseCase = GetRecoveryMissionUseCase { error("get should not be called") },
            completeRecoveryMissionUseCase = CompleteRecoveryMissionUseCase { error("complete should not be called") },
            submitRecoveryMissionUseCase = SubmitRecoveryMissionUseCase { error("submit should not be called") },
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.create(CreateRecoveryMissionRequest(diagnosticSessionId = "diagnostic-session-1"))

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("diagnostic-session-1", capturedCommand?.diagnosticSessionId)
        assertEquals("mission-1", response.body?.data?.id)
        assertEquals(3, response.body?.data?.hints?.size)
    }

    @Test
    fun `get returns recovery mission`() {
        var capturedCommand: GetRecoveryMissionCommand? = null
        val controller = RecoveryMissionController(
            createRecoveryMissionUseCase = CreateRecoveryMissionUseCase { error("create should not be called") },
            getRecoveryMissionUseCase = GetRecoveryMissionUseCase { command ->
                capturedCommand = command
                recoveryMissionResult()
            },
            completeRecoveryMissionUseCase = CompleteRecoveryMissionUseCase { error("complete should not be called") },
            submitRecoveryMissionUseCase = SubmitRecoveryMissionUseCase { error("submit should not be called") },
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.get(recoveryMissionId = "mission-1")

        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("mission-1", capturedCommand?.recoveryMissionId)
        assertEquals("일차함수 기울기 10분 복구 미션", response.data?.title)
    }

    @Test
    fun `complete marks recovery mission completed`() {
        var capturedCommand: CompleteRecoveryMissionCommand? = null
        val controller = RecoveryMissionController(
            createRecoveryMissionUseCase = CreateRecoveryMissionUseCase { error("create should not be called") },
            getRecoveryMissionUseCase = GetRecoveryMissionUseCase { error("get should not be called") },
            completeRecoveryMissionUseCase = CompleteRecoveryMissionUseCase { command ->
                capturedCommand = command
                recoveryMissionResult().copy(
                    status = RecoveryMissionStatus.COMPLETED,
                    completedAt = Instant.parse("2026-04-29T03:10:00Z"),
                )
            },
            submitRecoveryMissionUseCase = SubmitRecoveryMissionUseCase { error("submit should not be called") },
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.complete(recoveryMissionId = "mission-1")

        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("mission-1", capturedCommand?.recoveryMissionId)
        assertEquals(RecoveryMissionStatus.COMPLETED, response.data?.status)
    }

    @Test
    fun `submit stores answer and returns feedback`() {
        var capturedCommand: SubmitRecoveryMissionCommand? = null
        val controller = RecoveryMissionController(
            createRecoveryMissionUseCase = CreateRecoveryMissionUseCase { error("create should not be called") },
            getRecoveryMissionUseCase = GetRecoveryMissionUseCase { error("get should not be called") },
            completeRecoveryMissionUseCase = CompleteRecoveryMissionUseCase { error("complete should not be called") },
            submitRecoveryMissionUseCase = SubmitRecoveryMissionUseCase { command ->
                capturedCommand = command
                RecoveryMissionSubmissionResult(
                    id = "submission-1",
                    recoveryMissionId = "mission-1",
                    feedbackTitle = "복구 기록 완료",
                    feedbackMessage = "풀이 과정을 말로 남겼어요.",
                    nextAction = "다음 약점 개념 미션 이어가기",
                    submittedAt = Instant.parse("2026-04-29T03:10:00Z"),
                    mission = recoveryMissionResult().copy(
                        status = RecoveryMissionStatus.COMPLETED,
                        completedAt = Instant.parse("2026-04-29T03:10:00Z"),
                    ),
                )
            },
            businessStageBetaEventTracker = noOpBusinessStageBetaEventTracker(),
        )
        authenticate(emailVerified = true)

        val response = controller.submit(
            recoveryMissionId = "mission-1",
            request = SubmitRecoveryMissionRequest(answerText = "x 앞의 숫자가 기울기라서 -3을 찾았습니다."),
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals("mission-1", capturedCommand?.recoveryMissionId)
        assertEquals("submission-1", response.body?.data?.id)
        assertEquals(RecoveryMissionStatus.COMPLETED, response.body?.data?.mission?.status)
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

    private fun recoveryMissionResult(): RecoveryMissionResult {
        return RecoveryMissionResult(
            id = "mission-1",
            diagnosticSessionId = "diagnostic-session-1",
            conceptTag = "linear_function_slope",
            title = "일차함수 기울기 10분 복구 미션",
            prompt = "y = ax + b에서 기울기가 어떤 숫자인지 찾는 연습만 해요.",
            hints = listOf(
                "x 앞에 붙은 숫자를 먼저 찾아봐요.",
                "부호도 같이 봐야 해요.",
                "상수항은 시작 높이일 뿐이에요.",
            ),
            status = RecoveryMissionStatus.IN_PROGRESS,
            estimatedMinutes = 10,
            createdAt = Instant.parse("2026-04-29T03:00:00Z"),
            completedAt = null,
        )
    }
}

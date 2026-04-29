package com.example.naroo.learning.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.learning.port.`in`.GetLearningHomeCommand
import com.example.naroo.learning.port.`in`.GetLearningHomeUseCase
import com.example.naroo.learning.port.`in`.LearningHomeDiagnosticResult
import com.example.naroo.learning.port.`in`.LearningHomeMissionResult
import com.example.naroo.learning.port.`in`.LearningHomeNextAction
import com.example.naroo.learning.port.`in`.LearningHomeProgressResult
import com.example.naroo.learning.port.`in`.LearningHomeResult
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

class LearningHomeControllerTest {
    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns learning home for current user`() {
        var capturedCommand: GetLearningHomeCommand? = null
        val controller = LearningHomeController(
            getLearningHomeUseCase = GetLearningHomeUseCase { command ->
                capturedCommand = command
                learningHomeResult()
            },
        )
        authenticate(emailVerified = true)

        val response = controller.get()

        assertEquals("user-1", capturedCommand?.userId)
        assertEquals(true, capturedCommand?.emailVerified)
        assertEquals("나루", response.data?.user?.nickname)
        assertEquals(LearningHomeNextAction.CONTINUE_RECOVERY_MISSION, response.data?.nextAction)
        assertEquals("mission-1", response.data?.todayMission?.id)
    }

    private fun authenticate(emailVerified: Boolean) {
        val authentication = JwtAuthentication(
            tokenId = "token-1",
            userId = "user-1",
            loginId = "student01",
            emailVerified = emailVerified,
            nickname = "나루",
        )
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(authentication, null, emptyList())
    }

    private fun learningHomeResult(): LearningHomeResult {
        return LearningHomeResult(
            nextAction = LearningHomeNextAction.CONTINUE_RECOVERY_MISSION,
            latestDiagnostic = LearningHomeDiagnosticResult(
                diagnosticSessionId = "diagnostic-session-1",
                mathArea = MathArea.FUNCTION,
                status = DiagnosticSessionStatus.COMPLETED,
                totalQuestionCount = 2,
                correctCount = 1,
                wrongCount = 0,
                unknownCount = 1,
                weakLinks = listOf("linear_function_slope"),
                primaryRecoveryConcept = "linear_function_slope",
                summary = "전체가 무너진 게 아니에요.",
                createdAt = Instant.parse("2026-04-29T02:00:00Z"),
            ),
            todayMission = LearningHomeMissionResult(
                id = "mission-1",
                diagnosticSessionId = "diagnostic-session-1",
                conceptTag = "linear_function_slope",
                title = "일차함수 기울기 10분 복구 미션",
                status = RecoveryMissionStatus.IN_PROGRESS,
                estimatedMinutes = 10,
                createdAt = Instant.parse("2026-04-29T03:00:00Z"),
                completedAt = null,
            ),
            progress = LearningHomeProgressResult(
                completedMissionCount = 0,
                inProgressMissionCount = 1,
            ),
        )
    }
}

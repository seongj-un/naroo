package com.example.naroo.learning.application.service

import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.learning.port.`in`.GetLearningHomeCommand
import com.example.naroo.learning.port.`in`.LearningHomeNextAction
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class GetLearningHomeServiceTest {
    @Test
    fun `returns continue mission home when mission is in progress`() {
        val service = GetLearningHomeService(
            diagnosticResultRepositoryPort = FakeLearningHomeDiagnosticResultRepository(diagnosticResult()),
            recoveryMissionRepositoryPort = FakeLearningHomeRecoveryMissionRepository(
                missions = listOf(recoveryMission()),
            ),
        )

        val result = service.get(GetLearningHomeCommand(userId = "user-1", emailVerified = true))

        assertEquals(LearningHomeNextAction.CONTINUE_RECOVERY_MISSION, result.nextAction)
        assertEquals("diagnostic-session-1", result.latestDiagnostic?.diagnosticSessionId)
        assertEquals("mission-1", result.todayMission?.id)
        assertEquals("mission-1", result.latestMission?.id)
        assertEquals(1, result.progress.inProgressMissionCount)
    }

    @Test
    fun `asks user to start diagnostic when no result exists`() {
        val service = GetLearningHomeService(
            diagnosticResultRepositoryPort = FakeLearningHomeDiagnosticResultRepository(null),
            recoveryMissionRepositoryPort = FakeLearningHomeRecoveryMissionRepository(emptyList()),
        )

        val result = service.get(GetLearningHomeCommand(userId = "user-1", emailVerified = true))

        assertEquals(LearningHomeNextAction.START_DIAGNOSTIC, result.nextAction)
        assertNull(result.latestDiagnostic)
        assertNull(result.todayMission)
    }

    @Test
    fun `requires email verification before learning action`() {
        val service = GetLearningHomeService(
            diagnosticResultRepositoryPort = FakeLearningHomeDiagnosticResultRepository(diagnosticResult()),
            recoveryMissionRepositoryPort = FakeLearningHomeRecoveryMissionRepository(emptyList()),
        )

        val result = service.get(GetLearningHomeCommand(userId = "user-1", emailVerified = false))

        assertEquals(LearningHomeNextAction.EMAIL_VERIFICATION_REQUIRED, result.nextAction)
    }

    @Test
    fun `returns completed recovery state when all recovery missions are done`() {
        val completedMission = recoveryMission().copy(
            status = RecoveryMissionStatus.COMPLETED,
            completedAt = Instant.parse("2026-04-29T03:10:00Z"),
        )
        val service = GetLearningHomeService(
            diagnosticResultRepositoryPort = FakeLearningHomeDiagnosticResultRepository(diagnosticResult()),
            recoveryMissionRepositoryPort = FakeLearningHomeRecoveryMissionRepository(listOf(completedMission)),
        )

        val result = service.get(GetLearningHomeCommand(userId = "user-1", emailVerified = true))

        assertEquals(LearningHomeNextAction.RECOVERY_SERIES_COMPLETED, result.nextAction)
        assertNull(result.todayMission)
        assertEquals("mission-1", result.latestMission?.id)
        assertEquals(RecoveryMissionStatus.COMPLETED, result.latestMission?.status)
    }

    private fun diagnosticResult(): DiagnosticResult {
        return DiagnosticResult(
            diagnosticSessionId = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            mathArea = MathArea.FUNCTION,
            totalQuestionCount = 2,
            correctCount = 1,
            wrongCount = 0,
            unknownCount = 1,
            weakLinks = listOf("linear_function_slope"),
            primaryRecoveryConcept = "linear_function_slope",
            summary = "전체가 무너진 게 아니에요.",
            createdAt = Instant.parse("2026-04-29T02:00:00Z"),
        )
    }

    private fun recoveryMission(): RecoveryMission {
        return RecoveryMission(
            id = RecoveryMissionId("mission-1"),
            userId = UserId("user-1"),
            diagnosticSessionId = DiagnosticSessionId("diagnostic-session-1"),
            conceptTag = "linear_function_slope",
            title = "일차함수 기울기 10분 복구 미션",
            prompt = "y = ax + b에서 기울기가 어떤 숫자인지 찾는 연습만 해요.",
            hints = listOf("x 앞에 붙은 숫자를 먼저 찾아봐요."),
            status = RecoveryMissionStatus.IN_PROGRESS,
            estimatedMinutes = 10,
            createdAt = Instant.parse("2026-04-29T03:00:00Z"),
            completedAt = null,
        )
    }
}

private class FakeLearningHomeDiagnosticResultRepository(
    private val result: DiagnosticResult?,
) : DiagnosticResultRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return result?.takeIf { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findLatestByUserId(userId: UserId): DiagnosticResult? {
        return result?.takeIf { it.userId == userId }
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        error("result should not be saved")
    }
}

private class FakeLearningHomeRecoveryMissionRepository(
    private val missions: List<RecoveryMission>,
) : RecoveryMissionRepositoryPort {
    override fun findById(id: RecoveryMissionId): RecoveryMission? {
        return missions.firstOrNull { it.id == id }
    }

    override fun findByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): RecoveryMission? {
        return missions.firstOrNull { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findAllByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): List<RecoveryMission> {
        return missions.filter { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findLatestInProgressByUserId(userId: UserId): RecoveryMission? {
        return missions
            .filter { it.userId == userId && it.status == RecoveryMissionStatus.IN_PROGRESS }
            .maxByOrNull { it.createdAt }
    }

    override fun countByUserIdAndStatus(userId: UserId, status: RecoveryMissionStatus): Long {
        return missions.count { it.userId == userId && it.status == status }.toLong()
    }

    override fun save(mission: RecoveryMission): RecoveryMission {
        error("mission should not be saved")
    }
}

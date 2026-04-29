package com.example.naroo.recovery.application.service

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionCommand
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CompleteRecoveryMissionServiceTest {
    @Test
    fun `completes in progress recovery mission`() {
        val repository = CapturingCompleteRecoveryMissionRepository(recoveryMission())
        val service = CompleteRecoveryMissionService(
            recoveryMissionRepositoryPort = repository,
            clock = Clock.fixed(Instant.parse("2026-04-29T03:10:00Z"), ZoneOffset.UTC),
        )

        val result = service.complete(
            CompleteRecoveryMissionCommand(
                userId = "user-1",
                recoveryMissionId = "mission-1",
            ),
        )

        assertEquals(RecoveryMissionStatus.COMPLETED, result.status)
        assertEquals(Instant.parse("2026-04-29T03:10:00Z"), result.completedAt)
        assertEquals(RecoveryMissionStatus.COMPLETED, repository.saved.single().status)
    }

    @Test
    fun `rejects already completed mission`() {
        val service = CompleteRecoveryMissionService(
            recoveryMissionRepositoryPort = CapturingCompleteRecoveryMissionRepository(
                recoveryMission().copy(status = RecoveryMissionStatus.COMPLETED),
            ),
            clock = Clock.fixed(Instant.parse("2026-04-29T03:10:00Z"), ZoneOffset.UTC),
        )

        assertThrows(RecoveryMissionException.RecoveryMissionAlreadyCompleted::class.java) {
            service.complete(
                CompleteRecoveryMissionCommand(
                    userId = "user-1",
                    recoveryMissionId = "mission-1",
                ),
            )
        }
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

private class CapturingCompleteRecoveryMissionRepository(
    private var mission: RecoveryMission?,
) : RecoveryMissionRepositoryPort {
    val saved = mutableListOf<RecoveryMission>()

    override fun findById(id: RecoveryMissionId): RecoveryMission? {
        return mission?.takeIf { it.id == id }
    }

    override fun findByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): RecoveryMission? {
        return mission?.takeIf { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findAllByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): List<RecoveryMission> {
        return listOfNotNull(mission?.takeIf { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId })
    }

    override fun findLatestInProgressByUserId(userId: UserId): RecoveryMission? {
        return mission?.takeIf { it.userId == userId && it.status == RecoveryMissionStatus.IN_PROGRESS }
    }

    override fun countByUserIdAndStatus(userId: UserId, status: RecoveryMissionStatus): Long {
        return listOfNotNull(mission).count { it.userId == userId && it.status == status }.toLong()
    }

    override fun save(mission: RecoveryMission): RecoveryMission {
        this.mission = mission
        saved += mission
        return mission
    }
}

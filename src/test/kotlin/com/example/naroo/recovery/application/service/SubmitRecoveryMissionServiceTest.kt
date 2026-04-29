package com.example.naroo.recovery.application.service

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.domain.RecoveryMissionSubmission
import com.example.naroo.recovery.domain.RecoveryMissionSubmissionId
import com.example.naroo.recovery.port.`in`.SubmitRecoveryMissionCommand
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionIdGeneratorPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SubmitRecoveryMissionServiceTest {
    @Test
    fun `submits recovery mission answer and completes mission`() {
        val missionRepository = CapturingSubmitRecoveryMissionRepository(recoveryMission())
        val submissionRepository = CapturingRecoveryMissionSubmissionRepository()
        val service = SubmitRecoveryMissionService(
            recoveryMissionRepositoryPort = missionRepository,
            recoveryMissionSubmissionRepositoryPort = submissionRepository,
            recoveryMissionSubmissionIdGeneratorPort = RecoveryMissionSubmissionIdGeneratorPort {
                RecoveryMissionSubmissionId("submission-1")
            },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:10:00Z"), ZoneOffset.UTC),
        )

        val result = service.submit(
            SubmitRecoveryMissionCommand(
                userId = "user-1",
                recoveryMissionId = "mission-1",
                answerText = "x 앞에 붙은 숫자가 기울기라서 -3이라고 판단했습니다.",
            ),
        )

        assertEquals("submission-1", result.id)
        assertEquals("복구 기록 완료", result.feedbackTitle)
        assertEquals(RecoveryMissionStatus.COMPLETED, result.mission.status)
        assertEquals(RecoveryMissionStatus.COMPLETED, missionRepository.saved.single().status)
        assertEquals("x 앞에 붙은 숫자가 기울기라서 -3이라고 판단했습니다.", submissionRepository.saved.single().answerText)
    }

    @Test
    fun `rejects blank submission`() {
        val service = SubmitRecoveryMissionService(
            recoveryMissionRepositoryPort = CapturingSubmitRecoveryMissionRepository(recoveryMission()),
            recoveryMissionSubmissionRepositoryPort = CapturingRecoveryMissionSubmissionRepository(),
            recoveryMissionSubmissionIdGeneratorPort = RecoveryMissionSubmissionIdGeneratorPort {
                RecoveryMissionSubmissionId("submission-1")
            },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:10:00Z"), ZoneOffset.UTC),
        )

        assertThrows(RecoveryMissionException.InvalidRecoveryMissionSubmission::class.java) {
            service.submit(
                SubmitRecoveryMissionCommand(
                    userId = "user-1",
                    recoveryMissionId = "mission-1",
                    answerText = "   ",
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

private class CapturingSubmitRecoveryMissionRepository(
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

    override fun save(mission: RecoveryMission): RecoveryMission {
        this.mission = mission
        saved += mission
        return mission
    }
}

private class CapturingRecoveryMissionSubmissionRepository(
    private var submission: RecoveryMissionSubmission? = null,
) : RecoveryMissionSubmissionRepositoryPort {
    val saved = mutableListOf<RecoveryMissionSubmission>()

    override fun findByRecoveryMissionId(recoveryMissionId: RecoveryMissionId): RecoveryMissionSubmission? {
        return submission?.takeIf { it.recoveryMissionId == recoveryMissionId }
    }

    override fun save(submission: RecoveryMissionSubmission): RecoveryMissionSubmission {
        this.submission = submission
        saved += submission
        return submission
    }
}

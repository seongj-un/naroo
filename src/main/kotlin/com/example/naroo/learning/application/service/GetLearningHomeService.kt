package com.example.naroo.learning.application.service

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.learning.port.`in`.GetLearningHomeCommand
import com.example.naroo.learning.port.`in`.GetLearningHomeUseCase
import com.example.naroo.learning.port.`in`.LearningHomeDiagnosticResult
import com.example.naroo.learning.port.`in`.LearningHomeMissionResult
import com.example.naroo.learning.port.`in`.LearningHomeNextAction
import com.example.naroo.learning.port.`in`.LearningHomeProgressResult
import com.example.naroo.learning.port.`in`.LearningHomeResult
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class GetLearningHomeService(
    private val diagnosticResultRepositoryPort: DiagnosticResultRepositoryPort,
    private val recoveryMissionRepositoryPort: RecoveryMissionRepositoryPort,
) : GetLearningHomeUseCase {
    override fun get(command: GetLearningHomeCommand): LearningHomeResult {
        val userId = UserId(command.userId)
        val latestDiagnostic = diagnosticResultRepositoryPort.findLatestByUserId(userId)
        val todayMission = recoveryMissionRepositoryPort.findLatestInProgressByUserId(userId)
        val progress = LearningHomeProgressResult(
            completedMissionCount = recoveryMissionRepositoryPort.countByUserIdAndStatus(
                userId,
                RecoveryMissionStatus.COMPLETED,
            ),
            inProgressMissionCount = recoveryMissionRepositoryPort.countByUserIdAndStatus(
                userId,
                RecoveryMissionStatus.IN_PROGRESS,
            ),
        )

        return LearningHomeResult(
            nextAction = nextAction(command.emailVerified, latestDiagnostic != null, todayMission != null),
            latestDiagnostic = latestDiagnostic?.let {
                LearningHomeDiagnosticResult(
                    diagnosticSessionId = it.diagnosticSessionId.value,
                    mathArea = it.mathArea,
                    status = DiagnosticSessionStatus.COMPLETED,
                    totalQuestionCount = it.totalQuestionCount,
                    correctCount = it.correctCount,
                    wrongCount = it.wrongCount,
                    unknownCount = it.unknownCount,
                    weakLinks = it.weakLinks,
                    primaryRecoveryConcept = it.primaryRecoveryConcept,
                    summary = it.summary,
                    createdAt = it.createdAt,
                )
            },
            todayMission = todayMission?.toLearningHomeMissionResult(),
            progress = progress,
        )
    }

    private fun nextAction(
        emailVerified: Boolean,
        hasDiagnosticResult: Boolean,
        hasInProgressMission: Boolean,
    ): LearningHomeNextAction {
        if (!emailVerified) {
            return LearningHomeNextAction.EMAIL_VERIFICATION_REQUIRED
        }
        if (hasInProgressMission) {
            return LearningHomeNextAction.CONTINUE_RECOVERY_MISSION
        }
        if (hasDiagnosticResult) {
            return LearningHomeNextAction.CREATE_RECOVERY_MISSION
        }
        return LearningHomeNextAction.START_DIAGNOSTIC
    }

    private fun RecoveryMission.toLearningHomeMissionResult(): LearningHomeMissionResult {
        return LearningHomeMissionResult(
            id = id.value,
            diagnosticSessionId = diagnosticSessionId.value,
            conceptTag = conceptTag,
            title = title,
            status = status,
            estimatedMinutes = estimatedMinutes,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }
}

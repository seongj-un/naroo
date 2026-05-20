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
        val missionsForLatestDiagnostic = latestDiagnostic?.let {
            recoveryMissionRepositoryPort.findAllByUserIdAndDiagnosticSessionId(userId, it.diagnosticSessionId)
        }.orEmpty()
        val todayMission = missionsForLatestDiagnostic
            .filter { it.status == RecoveryMissionStatus.IN_PROGRESS }
            .maxByOrNull { it.createdAt }
        val latestMission = missionsForLatestDiagnostic.maxByOrNull { it.createdAt }
        val hasRemainingRecoveryMission = latestDiagnostic?.hasRemainingRecoveryMission(missionsForLatestDiagnostic) ?: false
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
            nextAction = nextAction(
                emailVerified = command.emailVerified,
                hasDiagnosticResult = latestDiagnostic != null,
                hasInProgressMission = todayMission != null,
                hasRemainingRecoveryMission = hasRemainingRecoveryMission,
                hasCompletedRecoverySeries = latestMission?.status == RecoveryMissionStatus.COMPLETED,
            ),
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
            latestMission = latestMission?.toLearningHomeMissionResult(),
            progress = progress,
        )
    }

    private fun nextAction(
        emailVerified: Boolean,
        hasDiagnosticResult: Boolean,
        hasInProgressMission: Boolean,
        hasRemainingRecoveryMission: Boolean,
        hasCompletedRecoverySeries: Boolean,
    ): LearningHomeNextAction {
        if (!emailVerified) {
            return LearningHomeNextAction.EMAIL_VERIFICATION_REQUIRED
        }
        if (hasInProgressMission) {
            return LearningHomeNextAction.CONTINUE_RECOVERY_MISSION
        }
        if (hasRemainingRecoveryMission) {
            return LearningHomeNextAction.CREATE_RECOVERY_MISSION
        }
        if (hasCompletedRecoverySeries) {
            return LearningHomeNextAction.RECOVERY_SERIES_COMPLETED
        }
        if (hasDiagnosticResult) {
            return LearningHomeNextAction.CREATE_RECOVERY_MISSION
        }
        return LearningHomeNextAction.START_DIAGNOSTIC
    }

    private fun com.example.naroo.diagnostic.domain.DiagnosticResult.hasRemainingRecoveryMission(
        existingMissions: List<RecoveryMission>,
    ): Boolean {
        val createdConcepts = existingMissions.map { it.conceptTag }.toSet()
        return orderedRecoveryConcepts().any { it !in createdConcepts }
    }

    private fun com.example.naroo.diagnostic.domain.DiagnosticResult.orderedRecoveryConcepts(): List<String> {
        return buildList {
            weakLinks.forEach { concept ->
                if (concept !in this) {
                    add(concept)
                }
            }
            if (primaryRecoveryConcept !in this) {
                add(primaryRecoveryConcept)
            }
        }
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

package com.example.naroo.learning.port.`in`

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import java.time.Instant

fun interface GetLearningHomeUseCase {
    fun get(command: GetLearningHomeCommand): LearningHomeResult
}

data class GetLearningHomeCommand(
    val userId: String,
    val emailVerified: Boolean,
)

data class LearningHomeResult(
    val nextAction: LearningHomeNextAction,
    val latestDiagnostic: LearningHomeDiagnosticResult?,
    val todayMission: LearningHomeMissionResult?,
    val latestMission: LearningHomeMissionResult?,
    val progress: LearningHomeProgressResult,
)

enum class LearningHomeNextAction {
    EMAIL_VERIFICATION_REQUIRED,
    START_DIAGNOSTIC,
    CREATE_RECOVERY_MISSION,
    CONTINUE_RECOVERY_MISSION,
    RECOVERY_SERIES_COMPLETED,
}

data class LearningHomeDiagnosticResult(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
    val createdAt: Instant,
)

data class LearningHomeMissionResult(
    val id: String,
    val diagnosticSessionId: String,
    val conceptTag: String,
    val title: String,
    val status: RecoveryMissionStatus,
    val estimatedMinutes: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
)

data class LearningHomeProgressResult(
    val completedMissionCount: Long,
    val inProgressMissionCount: Long,
)

package com.example.naroo.recovery.port.`in`

import java.time.Instant

fun interface SubmitRecoveryMissionUseCase {
    fun submit(command: SubmitRecoveryMissionCommand): RecoveryMissionSubmissionResult
}

data class SubmitRecoveryMissionCommand(
    val userId: String,
    val recoveryMissionId: String,
    val answerText: String,
)

data class RecoveryMissionSubmissionResult(
    val id: String,
    val recoveryMissionId: String,
    val feedbackTitle: String,
    val feedbackMessage: String,
    val nextAction: String,
    val submittedAt: Instant,
    val mission: RecoveryMissionResult,
)

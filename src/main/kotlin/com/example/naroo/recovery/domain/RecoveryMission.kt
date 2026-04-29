package com.example.naroo.recovery.domain

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.user.domain.UserId
import java.time.Instant

data class RecoveryMission(
    val id: RecoveryMissionId,
    val userId: UserId,
    val diagnosticSessionId: DiagnosticSessionId,
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val status: RecoveryMissionStatus,
    val estimatedMinutes: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
)

data class RecoveryMissionSubmission(
    val id: RecoveryMissionSubmissionId,
    val recoveryMissionId: RecoveryMissionId,
    val userId: UserId,
    val answerText: String,
    val feedbackTitle: String,
    val feedbackMessage: String,
    val nextAction: String,
    val submittedAt: Instant,
)

@JvmInline
value class RecoveryMissionId(val value: String) {
    init {
        require(value.isNotBlank()) { "recoveryMissionId must not be blank" }
    }
}

@JvmInline
value class RecoveryMissionSubmissionId(val value: String) {
    init {
        require(value.isNotBlank()) { "recoveryMissionSubmissionId must not be blank" }
    }
}

enum class RecoveryMissionStatus {
    IN_PROGRESS,
    COMPLETED,
}

package com.example.naroo.recovery.port.`in`

import com.example.naroo.recovery.domain.RecoveryMissionStatus
import java.time.Instant

fun interface CreateRecoveryMissionUseCase {
    fun create(command: CreateRecoveryMissionCommand): RecoveryMissionResult
}

data class CreateRecoveryMissionCommand(
    val userId: String,
    val diagnosticSessionId: String,
)

data class RecoveryMissionResult(
    val id: String,
    val diagnosticSessionId: String,
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val status: RecoveryMissionStatus,
    val estimatedMinutes: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
)

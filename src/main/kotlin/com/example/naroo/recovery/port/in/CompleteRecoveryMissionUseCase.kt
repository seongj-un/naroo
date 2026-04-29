package com.example.naroo.recovery.port.`in`

fun interface CompleteRecoveryMissionUseCase {
    fun complete(command: CompleteRecoveryMissionCommand): RecoveryMissionResult
}

data class CompleteRecoveryMissionCommand(
    val userId: String,
    val recoveryMissionId: String,
)

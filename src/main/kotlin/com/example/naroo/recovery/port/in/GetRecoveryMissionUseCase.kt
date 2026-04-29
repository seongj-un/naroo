package com.example.naroo.recovery.port.`in`

fun interface GetRecoveryMissionUseCase {
    fun get(command: GetRecoveryMissionCommand): RecoveryMissionResult
}

data class GetRecoveryMissionCommand(
    val userId: String,
    val recoveryMissionId: String,
)

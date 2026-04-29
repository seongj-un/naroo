package com.example.naroo.recovery.application.service

import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class GetRecoveryMissionService(
    private val recoveryMissionRepositoryPort: RecoveryMissionRepositoryPort,
) : GetRecoveryMissionUseCase {
    override fun get(command: GetRecoveryMissionCommand): RecoveryMissionResult {
        val userId = UserId(command.userId)
        val mission = recoveryMissionRepositoryPort.findById(RecoveryMissionId(command.recoveryMissionId))
            ?.takeIf { it.userId == userId }
            ?: throw RecoveryMissionException.RecoveryMissionNotFound

        return mission.toResult()
    }
}

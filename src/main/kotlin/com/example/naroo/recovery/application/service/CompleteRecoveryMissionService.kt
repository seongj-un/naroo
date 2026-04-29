package com.example.naroo.recovery.application.service

import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class CompleteRecoveryMissionService(
    private val recoveryMissionRepositoryPort: RecoveryMissionRepositoryPort,
    private val clock: Clock,
) : CompleteRecoveryMissionUseCase {
    override fun complete(command: CompleteRecoveryMissionCommand): RecoveryMissionResult {
        val userId = UserId(command.userId)
        val mission = recoveryMissionRepositoryPort.findById(RecoveryMissionId(command.recoveryMissionId))
            ?.takeIf { it.userId == userId }
            ?: throw RecoveryMissionException.RecoveryMissionNotFound

        if (mission.status == RecoveryMissionStatus.COMPLETED) {
            throw RecoveryMissionException.RecoveryMissionAlreadyCompleted
        }

        return recoveryMissionRepositoryPort.save(
            mission.copy(
                status = RecoveryMissionStatus.COMPLETED,
                completedAt = Instant.now(clock),
            ),
        ).toResult()
    }
}

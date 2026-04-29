package com.example.naroo.recovery.adapter.`out`.persistence

import com.example.naroo.recovery.domain.RecoveryMissionSubmissionId
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionIdGeneratorPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidRecoveryMissionSubmissionIdGeneratorAdapter : RecoveryMissionSubmissionIdGeneratorPort {
    override fun generate(): RecoveryMissionSubmissionId {
        return RecoveryMissionSubmissionId(UUID.randomUUID().toString())
    }
}

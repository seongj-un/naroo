package com.example.naroo.recovery.adapter.`out`.persistence

import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.port.`out`.RecoveryMissionIdGeneratorPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidRecoveryMissionIdGeneratorAdapter : RecoveryMissionIdGeneratorPort {
    override fun generate(): RecoveryMissionId {
        return RecoveryMissionId(UUID.randomUUID().toString())
    }
}

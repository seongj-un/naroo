package com.example.naroo.recovery.port.`out`

import com.example.naroo.recovery.domain.RecoveryMissionId

fun interface RecoveryMissionIdGeneratorPort {
    fun generate(): RecoveryMissionId
}

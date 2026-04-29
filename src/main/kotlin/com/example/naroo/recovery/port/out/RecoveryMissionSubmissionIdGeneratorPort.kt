package com.example.naroo.recovery.port.`out`

import com.example.naroo.recovery.domain.RecoveryMissionSubmissionId

fun interface RecoveryMissionSubmissionIdGeneratorPort {
    fun generate(): RecoveryMissionSubmissionId
}

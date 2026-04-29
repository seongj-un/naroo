package com.example.naroo.recovery.port.`out`

import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionSubmission

interface RecoveryMissionSubmissionRepositoryPort {
    fun findByRecoveryMissionId(recoveryMissionId: RecoveryMissionId): RecoveryMissionSubmission?
    fun save(submission: RecoveryMissionSubmission): RecoveryMissionSubmission
}

package com.example.naroo.recovery.application.service

import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult

fun RecoveryMission.toResult(): RecoveryMissionResult {
    return RecoveryMissionResult(
        id = id.value,
        diagnosticSessionId = diagnosticSessionId.value,
        conceptTag = conceptTag,
        title = title,
        prompt = prompt,
        hints = hints,
        status = status,
        estimatedMinutes = estimatedMinutes,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}

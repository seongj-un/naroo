package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.ListRecoveryMissionTemplateContentsUseCase
import com.example.naroo.content.port.`in`.RecoveryMissionTemplateContentResult
import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListRecoveryMissionTemplateContentsService(
    private val recoveryMissionTemplateRepositoryPort: RecoveryMissionTemplateRepositoryPort,
) : ListRecoveryMissionTemplateContentsUseCase {
    override fun list(): List<RecoveryMissionTemplateContentResult> {
        return recoveryMissionTemplateRepositoryPort.findAll().map { it.toResult() }
    }

    private fun RecoveryMissionTemplate.toResult(): RecoveryMissionTemplateContentResult {
        return RecoveryMissionTemplateContentResult(
            conceptTag = conceptTag,
            title = title,
            prompt = prompt,
            hints = hints,
            estimatedMinutes = estimatedMinutes,
            status = status.name,
        )
    }
}

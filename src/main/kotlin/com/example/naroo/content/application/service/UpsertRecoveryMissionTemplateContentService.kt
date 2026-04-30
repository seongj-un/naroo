package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.RecoveryMissionTemplateContentResult
import com.example.naroo.content.port.`in`.UpsertRecoveryMissionTemplateContentCommand
import com.example.naroo.content.port.`in`.UpsertRecoveryMissionTemplateContentUseCase
import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.application.service.RecoveryMissionTemplateStatus
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.springframework.stereotype.Service

@Service
class UpsertRecoveryMissionTemplateContentService(
    private val recoveryMissionTemplateRepositoryPort: RecoveryMissionTemplateRepositoryPort,
) : UpsertRecoveryMissionTemplateContentUseCase {
    override fun upsert(command: UpsertRecoveryMissionTemplateContentCommand): RecoveryMissionTemplateContentResult {
        require(command.hints.isNotEmpty()) { "hints must not be empty" }
        val template = RecoveryMissionTemplate(
            conceptTag = command.conceptTag.trim().also { require(it.isNotBlank()) },
            title = command.title.trim().also { require(it.isNotBlank()) },
            prompt = command.prompt.trim().also { require(it.isNotBlank()) },
            hints = command.hints.map { it.trim().also { hint -> require(hint.isNotBlank()) } },
            estimatedMinutes = command.estimatedMinutes.also { require(it > 0) },
            status = RecoveryMissionTemplateStatus.valueOf(command.status.trim().uppercase()),
        )

        return recoveryMissionTemplateRepositoryPort.save(template).toResult()
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

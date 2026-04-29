package com.example.naroo.recovery.port.`out`

import com.example.naroo.recovery.application.service.RecoveryMissionTemplate

interface RecoveryMissionTemplateRepositoryPort {
    fun findByConceptTag(conceptTag: String): RecoveryMissionTemplate?
    fun findAll(): List<RecoveryMissionTemplate>
}

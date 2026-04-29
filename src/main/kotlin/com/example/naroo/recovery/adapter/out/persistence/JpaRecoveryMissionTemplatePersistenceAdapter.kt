package com.example.naroo.recovery.adapter.`out`.persistence

import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaRecoveryMissionTemplatePersistenceAdapter(
    private val repository: SpringDataRecoveryMissionTemplateJpaRepository,
) : RecoveryMissionTemplateRepositoryPort {
    override fun findByConceptTag(conceptTag: String): RecoveryMissionTemplate? {
        return repository.findById(conceptTag).map(RecoveryMissionTemplateJpaEntity::toDomain).orElse(null)
    }

    override fun findAll(): List<RecoveryMissionTemplate> {
        return repository.findAllByOrderByConceptTagAsc().map(RecoveryMissionTemplateJpaEntity::toDomain)
    }
}

interface SpringDataRecoveryMissionTemplateJpaRepository : JpaRepository<RecoveryMissionTemplateJpaEntity, String> {
    fun findAllByOrderByConceptTagAsc(): List<RecoveryMissionTemplateJpaEntity>
}

@Entity
@Table(name = "recovery_mission_templates")
class RecoveryMissionTemplateJpaEntity(
    @Id
    @Column(name = "concept_tag", nullable = false, length = 100)
    var conceptTag: String = "",

    @Column(name = "title", nullable = false, length = 120)
    var title: String = "",

    @Column(name = "prompt", nullable = false, length = 500)
    var prompt: String = "",

    @Column(name = "hints", nullable = false, length = 1000)
    var hints: String = "",

    @Column(name = "estimated_minutes", nullable = false)
    var estimatedMinutes: Int = 10,
) {
    fun toDomain(): RecoveryMissionTemplate {
        return RecoveryMissionTemplate(
            conceptTag = conceptTag,
            title = title,
            prompt = prompt,
            hints = hints.replace("\\n", "\n").split(HINT_SEPARATOR).filter { it.isNotBlank() },
            estimatedMinutes = estimatedMinutes,
        )
    }

    companion object {
        private const val HINT_SEPARATOR = "\n---\n"
    }
}

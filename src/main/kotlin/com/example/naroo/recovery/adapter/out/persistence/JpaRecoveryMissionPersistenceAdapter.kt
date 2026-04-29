package com.example.naroo.recovery.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.user.domain.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaRecoveryMissionPersistenceAdapter(
    private val repository: SpringDataRecoveryMissionJpaRepository,
) : RecoveryMissionRepositoryPort {
    override fun findById(id: RecoveryMissionId): RecoveryMission? {
        return repository.findById(id.value).map(RecoveryMissionJpaEntity::toDomain).orElse(null)
    }

    override fun findByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): RecoveryMission? {
        return repository.findByUserIdAndDiagnosticSessionId(userId.value, diagnosticSessionId.value)?.toDomain()
    }

    override fun save(mission: RecoveryMission): RecoveryMission {
        return repository.saveAndFlush(RecoveryMissionJpaEntity.from(mission)).toDomain()
    }
}

interface SpringDataRecoveryMissionJpaRepository : JpaRepository<RecoveryMissionJpaEntity, String> {
    fun findByUserIdAndDiagnosticSessionId(userId: String, diagnosticSessionId: String): RecoveryMissionJpaEntity?
}

@Entity
@Table(name = "recovery_missions")
class RecoveryMissionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",

    @Column(name = "diagnostic_session_id", nullable = false, length = 36)
    var diagnosticSessionId: String = "",

    @Column(name = "concept_tag", nullable = false, length = 100)
    var conceptTag: String = "",

    @Column(name = "title", nullable = false, length = 120)
    var title: String = "",

    @Column(name = "prompt", nullable = false, length = 500)
    var prompt: String = "",

    @Column(name = "hints", nullable = false, length = 1000)
    var hints: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: RecoveryMissionStatus = RecoveryMissionStatus.IN_PROGRESS,

    @Column(name = "estimated_minutes", nullable = false)
    var estimatedMinutes: Int = 10,

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,

    @Column(name = "completed_at", columnDefinition = "datetime(6)")
    var completedAt: Instant? = null,
) {
    fun toDomain(): RecoveryMission {
        return RecoveryMission(
            id = RecoveryMissionId(id),
            userId = UserId(userId),
            diagnosticSessionId = DiagnosticSessionId(diagnosticSessionId),
            conceptTag = conceptTag,
            title = title,
            prompt = prompt,
            hints = hints.split(HINT_SEPARATOR).filter { it.isNotBlank() },
            status = status,
            estimatedMinutes = estimatedMinutes,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }

    companion object {
        private const val HINT_SEPARATOR = "\n---\n"

        fun from(mission: RecoveryMission): RecoveryMissionJpaEntity {
            return RecoveryMissionJpaEntity(
                id = mission.id.value,
                userId = mission.userId.value,
                diagnosticSessionId = mission.diagnosticSessionId.value,
                conceptTag = mission.conceptTag,
                title = mission.title,
                prompt = mission.prompt,
                hints = mission.hints.joinToString(HINT_SEPARATOR),
                status = mission.status,
                estimatedMinutes = mission.estimatedMinutes,
                createdAt = mission.createdAt,
                completedAt = mission.completedAt,
            )
        }
    }
}

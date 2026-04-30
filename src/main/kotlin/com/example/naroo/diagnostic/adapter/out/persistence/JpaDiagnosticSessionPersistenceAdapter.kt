package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
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
class JpaDiagnosticSessionPersistenceAdapter(
    private val repository: SpringDataDiagnosticSessionJpaRepository,
) : DiagnosticSessionRepositoryPort {
    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return repository.findById(id.value).map(DiagnosticSessionJpaEntity::toDomain).orElse(null)
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        return repository.saveAndFlush(DiagnosticSessionJpaEntity.from(session)).toDomain()
    }
}

interface SpringDataDiagnosticSessionJpaRepository : JpaRepository<DiagnosticSessionJpaEntity, String>

@Entity
@Table(name = "diagnostic_sessions")
class DiagnosticSessionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",

    @Column(name = "starting_point_selection_id", nullable = false, length = 36)
    var startingPointSelectionId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", nullable = false, length = 64)
    var mathArea: MathArea = MathArea.FUNCTION,

    @Column(name = "question_snapshot_version", nullable = false)
    var questionSnapshotVersion: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: DiagnosticSessionStatus = DiagnosticSessionStatus.READY,

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    var updatedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId(id),
            userId = UserId(userId),
            startingPointSelectionId = StartingPointSelectionId(startingPointSelectionId),
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun from(session: DiagnosticSession): DiagnosticSessionJpaEntity {
            return DiagnosticSessionJpaEntity(
                id = session.id.value,
                userId = session.userId.value,
                startingPointSelectionId = session.startingPointSelectionId.value,
                mathArea = session.mathArea,
                questionSnapshotVersion = session.questionSnapshotVersion,
                status = session.status,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }
}

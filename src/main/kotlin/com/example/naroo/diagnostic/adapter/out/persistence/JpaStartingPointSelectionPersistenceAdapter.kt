package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointNote
import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
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
class JpaStartingPointSelectionPersistenceAdapter(
    private val repository: SpringDataStartingPointSelectionJpaRepository,
) : StartingPointSelectionRepositoryPort {
    override fun findByUserId(userId: UserId): StartingPointSelection? {
        return repository.findByUserId(userId.value)?.toDomain()
    }

    override fun save(selection: StartingPointSelection): StartingPointSelection {
        return repository.saveAndFlush(StartingPointSelectionJpaEntity.from(selection)).toDomain()
    }
}

interface SpringDataStartingPointSelectionJpaRepository : JpaRepository<StartingPointSelectionJpaEntity, String> {
    fun findByUserId(userId: String): StartingPointSelectionJpaEntity?
}

@Entity
@Table(name = "diagnostic_starting_points")
class StartingPointSelectionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    var userId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 32)
    var selectionType: StartingPointSelectionType = StartingPointSelectionType.WEAK_AREA,

    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", nullable = false, length = 64)
    var mathArea: MathArea = MathArea.FUNCTION,

    @Column(name = "note", length = 200)
    var note: String? = null,

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    var updatedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): StartingPointSelection {
        return StartingPointSelection(
            id = StartingPointSelectionId(id),
            userId = UserId(userId),
            selectionType = selectionType,
            mathArea = mathArea,
            note = StartingPointNote.fromNullable(note),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun from(selection: StartingPointSelection): StartingPointSelectionJpaEntity {
            return StartingPointSelectionJpaEntity(
                id = selection.id.value,
                userId = selection.userId.value,
                selectionType = selection.selectionType,
                mathArea = selection.mathArea,
                note = selection.note?.value,
                createdAt = selection.createdAt,
                updatedAt = selection.updatedAt,
            )
        }
    }
}

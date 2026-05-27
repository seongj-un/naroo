package com.example.naroo.betaops.adapter.`out`.persistence

import com.example.naroo.betaops.domain.BetaTesterProfile
import com.example.naroo.betaops.domain.BetaTesterTargetMatch
import com.example.naroo.betaops.port.`out`.BetaTesterProfileRepositoryPort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaBetaTesterProfilePersistenceAdapter(
    private val repository: SpringDataBetaTesterProfileJpaRepository,
) : BetaTesterProfileRepositoryPort {
    override fun findAll(): List<BetaTesterProfile> {
        return repository.findAllByOrderByUpdatedAtDesc().map(BetaTesterProfileJpaEntity::toDomain)
    }

    override fun findByUserId(userId: String): BetaTesterProfile? {
        return repository.findById(userId).map(BetaTesterProfileJpaEntity::toDomain).orElse(null)
    }

    override fun save(profile: BetaTesterProfile): BetaTesterProfile {
        return repository.saveAndFlush(BetaTesterProfileJpaEntity.from(profile)).toDomain()
    }
}

interface SpringDataBetaTesterProfileJpaRepository : JpaRepository<BetaTesterProfileJpaEntity, String> {
    fun findAllByOrderByUpdatedAtDesc(): List<BetaTesterProfileJpaEntity>
}

@Entity
@Table(
    name = "beta_tester_profiles",
    indexes = [
        Index(name = "idx_beta_tester_profiles_cohort_tag", columnList = "cohort_tag"),
        Index(name = "idx_beta_tester_profiles_target_match", columnList = "target_match"),
    ],
)
class BetaTesterProfileJpaEntity(
    @Id
    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",
    @Column(name = "cohort_tag", length = 64)
    var cohortTag: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "target_match", length = 32)
    var targetMatch: BetaTesterTargetMatch? = null,
    @Column(name = "operator_note", columnDefinition = "text")
    var operatorNote: String? = null,
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    var updatedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): BetaTesterProfile {
        return BetaTesterProfile(
            userId = userId,
            cohortTag = cohortTag,
            targetMatch = targetMatch,
            operatorNote = operatorNote,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun from(profile: BetaTesterProfile): BetaTesterProfileJpaEntity {
            return BetaTesterProfileJpaEntity(
                userId = profile.userId,
                cohortTag = profile.cohortTag,
                targetMatch = profile.targetMatch,
                operatorNote = profile.operatorNote,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
            )
        }
    }
}

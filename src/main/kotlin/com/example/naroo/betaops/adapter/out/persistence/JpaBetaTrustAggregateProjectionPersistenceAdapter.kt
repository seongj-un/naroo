package com.example.naroo.betaops.adapter.`out`.persistence

import com.example.naroo.betaops.domain.BetaTrustAggregateRow
import com.example.naroo.betaops.port.`out`.BetaTrustAggregateProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
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
class JpaBetaTrustAggregateProjectionPersistenceAdapter(
    private val repository: SpringDataBetaTrustAggregateRowJpaRepository,
) : BetaTrustAggregateProjectionRepositoryPort {
    override fun replaceAll(rows: List<BetaTrustAggregateRow>): List<BetaTrustAggregateRow> {
        repository.deleteAllInBatch()
        if (rows.isEmpty()) {
            return emptyList()
        }
        return repository.saveAllAndFlush(rows.map(BetaTrustAggregateRowJpaEntity::from)).map(BetaTrustAggregateRowJpaEntity::toDomain)
    }
}

interface SpringDataBetaTrustAggregateRowJpaRepository : JpaRepository<BetaTrustAggregateRowJpaEntity, String>

@Entity
@Table(
    name = "beta_trust_aggregate_rows",
    indexes = [
        Index(name = "idx_beta_trust_aggregate_rows_math_area", columnList = "math_area"),
        Index(name = "idx_beta_trust_aggregate_rows_snapshot_version", columnList = "question_snapshot_version"),
        Index(name = "idx_beta_trust_aggregate_rows_flow_variant", columnList = "flow_variant"),
    ],
)
class BetaTrustAggregateRowJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", length = 64)
    var mathArea: MathArea? = null,
    @Column(name = "question_snapshot_version")
    var questionSnapshotVersion: Int? = null,
    @Column(name = "flow_variant", length = 100)
    var flowVariant: String? = null,
    @Column(name = "result_copy_version", length = 100)
    var resultCopyVersion: String? = null,
    @Column(name = "primary_recovery_concept", length = 120)
    var primaryRecoveryConcept: String? = null,
    @Column(name = "feedback_count", nullable = false)
    var feedbackCount: Int = 0,
    @Column(name = "feels_right_count", nullable = false)
    var feelsRightCount: Int = 0,
    @Column(name = "unsure_count", nullable = false)
    var unsureCount: Int = 0,
    @Column(name = "last_event_at", nullable = false, columnDefinition = "datetime(6)")
    var lastEventAt: Instant = Instant.EPOCH,
    @Column(name = "projected_at", nullable = false, columnDefinition = "datetime(6)")
    var projectedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): BetaTrustAggregateRow {
        return BetaTrustAggregateRow(
            id = id,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            primaryRecoveryConcept = primaryRecoveryConcept,
            feedbackCount = feedbackCount,
            feelsRightCount = feelsRightCount,
            unsureCount = unsureCount,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }

    companion object {
        fun from(row: BetaTrustAggregateRow): BetaTrustAggregateRowJpaEntity {
            return BetaTrustAggregateRowJpaEntity(
                id = row.id,
                mathArea = row.mathArea,
                questionSnapshotVersion = row.questionSnapshotVersion,
                flowVariant = row.flowVariant,
                resultCopyVersion = row.resultCopyVersion,
                primaryRecoveryConcept = row.primaryRecoveryConcept,
                feedbackCount = row.feedbackCount,
                feelsRightCount = row.feelsRightCount,
                unsureCount = row.unsureCount,
                lastEventAt = row.lastEventAt,
                projectedAt = row.projectedAt,
            )
        }
    }
}

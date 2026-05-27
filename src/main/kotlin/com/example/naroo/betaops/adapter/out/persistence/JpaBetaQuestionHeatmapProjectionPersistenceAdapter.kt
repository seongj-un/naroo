package com.example.naroo.betaops.adapter.`out`.persistence

import com.example.naroo.betaops.domain.BetaQuestionHeatmapRow
import com.example.naroo.betaops.port.`out`.BetaQuestionHeatmapProjectionRepositoryPort
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
class JpaBetaQuestionHeatmapProjectionPersistenceAdapter(
    private val repository: SpringDataBetaQuestionHeatmapRowJpaRepository,
) : BetaQuestionHeatmapProjectionRepositoryPort {
    override fun replaceAll(rows: List<BetaQuestionHeatmapRow>): List<BetaQuestionHeatmapRow> {
        repository.deleteAllInBatch()
        if (rows.isEmpty()) {
            return emptyList()
        }
        return repository.saveAllAndFlush(rows.map(BetaQuestionHeatmapRowJpaEntity::from)).map(BetaQuestionHeatmapRowJpaEntity::toDomain)
    }
}

interface SpringDataBetaQuestionHeatmapRowJpaRepository : JpaRepository<BetaQuestionHeatmapRowJpaEntity, String>

@Entity
@Table(
    name = "beta_question_heatmap_rows",
    indexes = [
        Index(name = "idx_beta_question_heatmap_rows_math_area", columnList = "math_area"),
        Index(name = "idx_beta_question_heatmap_rows_question_id", columnList = "question_id"),
        Index(name = "idx_beta_question_heatmap_rows_snapshot_version", columnList = "question_snapshot_version"),
    ],
)
class BetaQuestionHeatmapRowJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String = "",
    @Column(name = "question_id", nullable = false, length = 120)
    var questionId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", length = 64)
    var mathArea: MathArea? = null,
    @Column(name = "concept_tag", length = 120)
    var conceptTag: String? = null,
    @Column(name = "question_snapshot_version")
    var questionSnapshotVersion: Int? = null,
    @Column(name = "flow_variant", length = 100)
    var flowVariant: String? = null,
    @Column(name = "display_order")
    var displayOrder: Int? = null,
    @Column(name = "question_shown_count", nullable = false)
    var questionShownCount: Int = 0,
    @Column(name = "answer_selected_count", nullable = false)
    var answerSelectedCount: Int = 0,
    @Column(name = "unknown_answer_count", nullable = false)
    var unknownAnswerCount: Int = 0,
    @Column(name = "abandoned_after_question_count", nullable = false)
    var abandonedAfterQuestionCount: Int = 0,
    @Column(name = "last_event_at", nullable = false, columnDefinition = "datetime(6)")
    var lastEventAt: Instant = Instant.EPOCH,
    @Column(name = "projected_at", nullable = false, columnDefinition = "datetime(6)")
    var projectedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): BetaQuestionHeatmapRow {
        return BetaQuestionHeatmapRow(
            id = id,
            questionId = questionId,
            mathArea = mathArea,
            conceptTag = conceptTag,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            displayOrder = displayOrder,
            questionShownCount = questionShownCount,
            answerSelectedCount = answerSelectedCount,
            unknownAnswerCount = unknownAnswerCount,
            abandonedAfterQuestionCount = abandonedAfterQuestionCount,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }

    companion object {
        fun from(row: BetaQuestionHeatmapRow): BetaQuestionHeatmapRowJpaEntity {
            return BetaQuestionHeatmapRowJpaEntity(
                id = row.id,
                questionId = row.questionId,
                mathArea = row.mathArea,
                conceptTag = row.conceptTag,
                questionSnapshotVersion = row.questionSnapshotVersion,
                flowVariant = row.flowVariant,
                displayOrder = row.displayOrder,
                questionShownCount = row.questionShownCount,
                answerSelectedCount = row.answerSelectedCount,
                unknownAnswerCount = row.unknownAnswerCount,
                abandonedAfterQuestionCount = row.abandonedAfterQuestionCount,
                lastEventAt = row.lastEventAt,
                projectedAt = row.projectedAt,
            )
        }
    }
}

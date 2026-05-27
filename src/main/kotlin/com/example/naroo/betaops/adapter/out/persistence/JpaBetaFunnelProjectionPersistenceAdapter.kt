package com.example.naroo.betaops.adapter.`out`.persistence

import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaFunnelRow
import com.example.naroo.betaops.port.`out`.BetaFunnelProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
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
class JpaBetaFunnelProjectionPersistenceAdapter(
    private val repository: SpringDataBetaFunnelRowJpaRepository,
) : BetaFunnelProjectionRepositoryPort {
    override fun replaceAll(rows: List<BetaFunnelRow>): List<BetaFunnelRow> {
        repository.deleteAllInBatch()
        if (rows.isEmpty()) {
            return emptyList()
        }
        return repository.saveAllAndFlush(rows.map(BetaFunnelRowJpaEntity::from)).map(BetaFunnelRowJpaEntity::toDomain)
    }

    override fun findAll(): List<BetaFunnelRow> {
        return repository.findAllByOrderByLastEventAtDesc().map(BetaFunnelRowJpaEntity::toDomain)
    }
}

interface SpringDataBetaFunnelRowJpaRepository : JpaRepository<BetaFunnelRowJpaEntity, String> {
    fun findAllByOrderByLastEventAtDesc(): List<BetaFunnelRowJpaEntity>
}

@Entity
@Table(
    name = "beta_funnel_rows",
    indexes = [
        Index(name = "idx_beta_funnel_rows_user_id", columnList = "user_id"),
        Index(name = "idx_beta_funnel_rows_math_area", columnList = "math_area"),
        Index(name = "idx_beta_funnel_rows_last_event_at", columnList = "last_event_at"),
    ],
)
class BetaFunnelRowJpaEntity(
    @Id
    @Column(name = "diagnostic_session_id", nullable = false, length = 36)
    var diagnosticSessionId: String = "",
    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", length = 64)
    var mathArea: MathArea? = null,
    @Column(name = "question_snapshot_version")
    var questionSnapshotVersion: Int? = null,
    @Column(name = "flow_variant", length = 100)
    var flowVariant: String? = null,
    @Column(name = "result_copy_version", length = 100)
    var resultCopyVersion: String? = null,
    @Column(name = "login_succeeded_at", columnDefinition = "datetime(6)")
    var loginSucceededAt: Instant? = null,
    @Column(name = "starting_point_selected_at", columnDefinition = "datetime(6)")
    var startingPointSelectedAt: Instant? = null,
    @Column(name = "diagnostic_session_created_at", columnDefinition = "datetime(6)")
    var diagnosticSessionCreatedAt: Instant? = null,
    @Column(name = "first_question_shown_at", columnDefinition = "datetime(6)")
    var firstQuestionShownAt: Instant? = null,
    @Column(name = "first_answer_selected_at", columnDefinition = "datetime(6)")
    var firstAnswerSelectedAt: Instant? = null,
    @Column(name = "diagnostic_submitted_at", columnDefinition = "datetime(6)")
    var diagnosticSubmittedAt: Instant? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "trust_feedback_choice", length = 32)
    var trustFeedbackChoice: DiagnosticResultTrustFeedbackChoice? = null,
    @Column(name = "trust_feedback_at", columnDefinition = "datetime(6)")
    var trustFeedbackAt: Instant? = null,
    @Column(name = "recovery_mission_created_at", columnDefinition = "datetime(6)")
    var recoveryMissionCreatedAt: Instant? = null,
    @Column(name = "recovery_mission_submitted_at", columnDefinition = "datetime(6)")
    var recoveryMissionSubmittedAt: Instant? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "last_event_type", nullable = false, length = 64)
    var lastEventType: BetaEventType = BetaEventType.AUTH_LOGIN_SUCCEEDED,
    @Column(name = "last_event_at", nullable = false, columnDefinition = "datetime(6)")
    var lastEventAt: Instant = Instant.EPOCH,
    @Column(name = "projected_at", nullable = false, columnDefinition = "datetime(6)")
    var projectedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): BetaFunnelRow {
        return BetaFunnelRow(
            diagnosticSessionId = diagnosticSessionId,
            userId = userId,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            loginSucceededAt = loginSucceededAt,
            startingPointSelectedAt = startingPointSelectedAt,
            diagnosticSessionCreatedAt = diagnosticSessionCreatedAt,
            firstQuestionShownAt = firstQuestionShownAt,
            firstAnswerSelectedAt = firstAnswerSelectedAt,
            diagnosticSubmittedAt = diagnosticSubmittedAt,
            trustFeedbackChoice = trustFeedbackChoice,
            trustFeedbackAt = trustFeedbackAt,
            recoveryMissionCreatedAt = recoveryMissionCreatedAt,
            recoveryMissionSubmittedAt = recoveryMissionSubmittedAt,
            lastEventType = lastEventType,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }

    companion object {
        fun from(row: BetaFunnelRow): BetaFunnelRowJpaEntity {
            return BetaFunnelRowJpaEntity(
                diagnosticSessionId = row.diagnosticSessionId,
                userId = row.userId,
                mathArea = row.mathArea,
                questionSnapshotVersion = row.questionSnapshotVersion,
                flowVariant = row.flowVariant,
                resultCopyVersion = row.resultCopyVersion,
                loginSucceededAt = row.loginSucceededAt,
                startingPointSelectedAt = row.startingPointSelectedAt,
                diagnosticSessionCreatedAt = row.diagnosticSessionCreatedAt,
                firstQuestionShownAt = row.firstQuestionShownAt,
                firstAnswerSelectedAt = row.firstAnswerSelectedAt,
                diagnosticSubmittedAt = row.diagnosticSubmittedAt,
                trustFeedbackChoice = row.trustFeedbackChoice,
                trustFeedbackAt = row.trustFeedbackAt,
                recoveryMissionCreatedAt = row.recoveryMissionCreatedAt,
                recoveryMissionSubmittedAt = row.recoveryMissionSubmittedAt,
                lastEventType = row.lastEventType,
                lastEventAt = row.lastEventAt,
                projectedAt = row.projectedAt,
            )
        }
    }
}

package com.example.naroo.betaops.adapter.`out`.persistence

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.example.naroo.betaops.port.`out`.DuplicateBetaEventIdempotencyKeyException
import com.example.naroo.diagnostic.domain.MathArea
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaBetaEventPersistenceAdapter(
    private val repository: SpringDataBetaEventJpaRepository,
) : BetaEventRepositoryPort {
    override fun save(event: BetaEvent): BetaEvent {
        try {
            return repository.saveAndFlush(BetaEventJpaEntity.from(event)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            if (matchesIdempotencyConstraint(exception)) {
                throw DuplicateBetaEventIdempotencyKeyException(event.idempotencyKey, exception)
            }
            throw exception
        }
    }

    private fun matchesIdempotencyConstraint(exception: DataIntegrityViolationException): Boolean {
        var current: Throwable? = exception
        while (current != null) {
            val message = current.message.orEmpty()
            if (message.contains("uq_beta_events_idempotency_key", ignoreCase = true) ||
                message.contains("idempotency_key", ignoreCase = true)
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }
}

interface SpringDataBetaEventJpaRepository : JpaRepository<BetaEventJpaEntity, String>

@Entity
@Table(
    name = "beta_events",
    indexes = [
        Index(name = "idx_beta_events_event_type", columnList = "event_type"),
        Index(name = "idx_beta_events_user_id", columnList = "user_id"),
        Index(name = "idx_beta_events_diagnostic_session_id", columnList = "diagnostic_session_id"),
        Index(name = "idx_beta_events_occurred_at", columnList = "occurred_at"),
    ],
)
class BetaEventJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    var eventType: BetaEventType = BetaEventType.AUTH_LOGIN_SUCCEEDED,
    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",
    @Column(name = "diagnostic_session_id", length = 36)
    var diagnosticSessionId: String? = null,
    @Column(name = "recovery_mission_id", length = 36)
    var recoveryMissionId: String? = null,
    @Column(name = "question_id", length = 120)
    var questionId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", length = 64)
    var mathArea: MathArea? = null,
    @Column(name = "question_snapshot_version")
    var questionSnapshotVersion: Int? = null,
    @Column(name = "flow_variant", length = 100)
    var flowVariant: String? = null,
    @Column(name = "result_copy_version", length = 100)
    var resultCopyVersion: String? = null,
    @Column(name = "idempotency_key", nullable = false, length = 191)
    var idempotencyKey: String = "",
    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    var payloadJson: String = "",
    @Column(name = "occurred_at", nullable = false, columnDefinition = "datetime(6)")
    var occurredAt: Instant = Instant.EPOCH,
    @Column(name = "received_at", nullable = false, columnDefinition = "datetime(6)")
    var receivedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): BetaEvent {
        return BetaEvent(
            id = id,
            eventType = eventType,
            userId = userId,
            diagnosticSessionId = diagnosticSessionId,
            recoveryMissionId = recoveryMissionId,
            questionId = questionId,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            idempotencyKey = idempotencyKey,
            payloadJson = payloadJson,
            occurredAt = occurredAt,
            receivedAt = receivedAt,
        )
    }

    companion object {
        fun from(event: BetaEvent): BetaEventJpaEntity {
            return BetaEventJpaEntity(
                id = event.id,
                eventType = event.eventType,
                userId = event.userId,
                diagnosticSessionId = event.diagnosticSessionId,
                recoveryMissionId = event.recoveryMissionId,
                questionId = event.questionId,
                mathArea = event.mathArea,
                questionSnapshotVersion = event.questionSnapshotVersion,
                flowVariant = event.flowVariant,
                resultCopyVersion = event.resultCopyVersion,
                idempotencyKey = event.idempotencyKey,
                payloadJson = event.payloadJson,
                occurredAt = event.occurredAt,
                receivedAt = event.receivedAt,
            )
        }
    }
}

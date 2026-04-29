package com.example.naroo.recovery.adapter.`out`.persistence

import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionSubmission
import com.example.naroo.recovery.domain.RecoveryMissionSubmissionId
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionRepositoryPort
import com.example.naroo.user.domain.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaRecoveryMissionSubmissionPersistenceAdapter(
    private val repository: SpringDataRecoveryMissionSubmissionJpaRepository,
) : RecoveryMissionSubmissionRepositoryPort {
    override fun findByRecoveryMissionId(recoveryMissionId: RecoveryMissionId): RecoveryMissionSubmission? {
        return repository.findByRecoveryMissionId(recoveryMissionId.value)?.toDomain()
    }

    override fun save(submission: RecoveryMissionSubmission): RecoveryMissionSubmission {
        return repository.saveAndFlush(RecoveryMissionSubmissionJpaEntity.from(submission)).toDomain()
    }
}

interface SpringDataRecoveryMissionSubmissionJpaRepository : JpaRepository<RecoveryMissionSubmissionJpaEntity, String> {
    fun findByRecoveryMissionId(recoveryMissionId: String): RecoveryMissionSubmissionJpaEntity?
}

@Entity
@Table(name = "recovery_mission_submissions")
class RecoveryMissionSubmissionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",

    @Column(name = "recovery_mission_id", nullable = false, length = 36)
    var recoveryMissionId: String = "",

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",

    @Column(name = "answer_text", nullable = false, length = 1000)
    var answerText: String = "",

    @Column(name = "feedback_title", nullable = false, length = 120)
    var feedbackTitle: String = "",

    @Column(name = "feedback_message", nullable = false, length = 500)
    var feedbackMessage: String = "",

    @Column(name = "next_action", nullable = false, length = 200)
    var nextAction: String = "",

    @Column(name = "submitted_at", nullable = false, columnDefinition = "datetime(6)")
    var submittedAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): RecoveryMissionSubmission {
        return RecoveryMissionSubmission(
            id = RecoveryMissionSubmissionId(id),
            recoveryMissionId = RecoveryMissionId(recoveryMissionId),
            userId = UserId(userId),
            answerText = answerText,
            feedbackTitle = feedbackTitle,
            feedbackMessage = feedbackMessage,
            nextAction = nextAction,
            submittedAt = submittedAt,
        )
    }

    companion object {
        fun from(submission: RecoveryMissionSubmission): RecoveryMissionSubmissionJpaEntity {
            return RecoveryMissionSubmissionJpaEntity(
                id = submission.id.value,
                recoveryMissionId = submission.recoveryMissionId.value,
                userId = submission.userId.value,
                answerText = submission.answerText,
                feedbackTitle = submission.feedbackTitle,
                feedbackMessage = submission.feedbackMessage,
                nextAction = submission.nextAction,
                submittedAt = submission.submittedAt,
            )
        }
    }
}

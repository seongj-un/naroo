package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticAnswer
import com.example.naroo.diagnostic.domain.DiagnosticAnswerId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.port.`out`.DiagnosticAnswerRepositoryPort
import com.example.naroo.user.domain.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaDiagnosticAnswerPersistenceAdapter(
    private val repository: SpringDataDiagnosticAnswerJpaRepository,
) : DiagnosticAnswerRepositoryPort {
    override fun existsByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): Boolean {
        return repository.existsByDiagnosticSessionId(diagnosticSessionId.value)
    }

    override fun saveAll(answers: List<DiagnosticAnswer>): List<DiagnosticAnswer> {
        return repository.saveAllAndFlush(answers.map(DiagnosticAnswerJpaEntity::from)).map { it.toDomain() }
    }
}

interface SpringDataDiagnosticAnswerJpaRepository : JpaRepository<DiagnosticAnswerJpaEntity, String> {
    fun existsByDiagnosticSessionId(diagnosticSessionId: String): Boolean
}

@Entity
@Table(name = "diagnostic_answers")
class DiagnosticAnswerJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 120)
    var id: String = "",

    @Column(name = "diagnostic_session_id", nullable = false, length = 36)
    var diagnosticSessionId: String = "",

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",

    @Column(name = "question_id", nullable = false, length = 100)
    var questionId: String = "",

    @Column(name = "selected_choice_id", nullable = false, length = 32)
    var selectedChoiceId: String = "",

    @Column(name = "correct_choice_id", nullable = false, length = 32)
    var correctChoiceId: String = "",

    @Column(name = "concept_tag", nullable = false, length = 100)
    var conceptTag: String = "",

    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean = false,

    @Column(name = "is_unknown", nullable = false)
    var isUnknown: Boolean = false,

    @Column(name = "answered_at", nullable = false, columnDefinition = "datetime(6)")
    var answeredAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): DiagnosticAnswer {
        return DiagnosticAnswer(
            id = DiagnosticAnswerId(id),
            diagnosticSessionId = DiagnosticSessionId(diagnosticSessionId),
            userId = UserId(userId),
            questionId = DiagnosticQuestionId(questionId),
            selectedChoiceId = DiagnosticQuestionChoiceId(selectedChoiceId),
            correctChoiceId = DiagnosticQuestionChoiceId(correctChoiceId),
            conceptTag = conceptTag,
            isCorrect = isCorrect,
            isUnknown = isUnknown,
            answeredAt = answeredAt,
        )
    }

    companion object {
        fun from(answer: DiagnosticAnswer): DiagnosticAnswerJpaEntity {
            return DiagnosticAnswerJpaEntity(
                id = answer.id.value,
                diagnosticSessionId = answer.diagnosticSessionId.value,
                userId = answer.userId.value,
                questionId = answer.questionId.value,
                selectedChoiceId = answer.selectedChoiceId.value,
                correctChoiceId = answer.correctChoiceId.value,
                conceptTag = answer.conceptTag,
                isCorrect = answer.isCorrect,
                isUnknown = answer.isUnknown,
                answeredAt = answer.answeredAt,
            )
        }
    }
}

package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionStatus
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaDiagnosticSessionQuestionSnapshotPersistenceAdapter(
    private val questionRepository: SpringDataDiagnosticSessionQuestionJpaRepository,
    private val choiceRepository: SpringDataDiagnosticSessionQuestionChoiceJpaRepository,
) : DiagnosticSessionQuestionSnapshotRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): List<DiagnosticQuestion> {
        val questions = questionRepository.findAllByDiagnosticSessionIdOrderByDisplayOrderAsc(diagnosticSessionId.value)
        if (questions.isEmpty()) {
            return emptyList()
        }

        val choicesByQuestionId = choiceRepository
            .findAllByDiagnosticSessionQuestionIdInOrderByDisplayOrderAsc(questions.map { it.id })
            .groupBy { it.diagnosticSessionQuestionId }

        return questions.map { question ->
            question.toDomain(choicesByQuestionId.getValue(question.id))
        }
    }

    override fun saveSnapshot(
        diagnosticSessionId: DiagnosticSessionId,
        questions: List<DiagnosticQuestion>,
    ): List<DiagnosticQuestion> {
        val questionEntities = questions.map { DiagnosticSessionQuestionJpaEntity.from(diagnosticSessionId, it) }
        val choiceEntities = questions.flatMap { question ->
            question.choices.mapIndexed { index, choice ->
                DiagnosticSessionQuestionChoiceJpaEntity.from(diagnosticSessionId, question, choice, index + 1)
            }
        }
        questionRepository.saveAllAndFlush(questionEntities)
        choiceRepository.saveAllAndFlush(choiceEntities)
        return questions
    }
}

interface SpringDataDiagnosticSessionQuestionJpaRepository : JpaRepository<DiagnosticSessionQuestionJpaEntity, String> {
    fun findAllByDiagnosticSessionIdOrderByDisplayOrderAsc(diagnosticSessionId: String): List<DiagnosticSessionQuestionJpaEntity>
}

interface SpringDataDiagnosticSessionQuestionChoiceJpaRepository :
    JpaRepository<DiagnosticSessionQuestionChoiceJpaEntity, String> {
    fun findAllByDiagnosticSessionQuestionIdInOrderByDisplayOrderAsc(
        diagnosticSessionQuestionIds: List<String>,
    ): List<DiagnosticSessionQuestionChoiceJpaEntity>
}

@Entity
@Table(name = "diagnostic_session_questions")
class DiagnosticSessionQuestionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 160)
    var id: String = "",

    @Column(name = "diagnostic_session_id", nullable = false, length = 36)
    var diagnosticSessionId: String = "",

    @Column(name = "question_id", nullable = false, length = 80)
    var questionId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", nullable = false, length = 64)
    var mathArea: MathArea = MathArea.FUNCTION,

    @Column(name = "prompt", nullable = false, length = 500)
    var prompt: String = "",

    @Column(name = "correct_choice_id", nullable = false, length = 40)
    var correctChoiceId: String = "",

    @Column(name = "concept_tag", nullable = false, length = 100)
    var conceptTag: String = "",

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,
) {
    fun toDomain(choices: List<DiagnosticSessionQuestionChoiceJpaEntity>): DiagnosticQuestion {
        return DiagnosticQuestion(
            id = DiagnosticQuestionId(questionId),
            mathArea = mathArea,
            prompt = prompt,
            choices = choices.map(DiagnosticSessionQuestionChoiceJpaEntity::toDomain),
            correctChoiceId = DiagnosticQuestionChoiceId(correctChoiceId),
            conceptTag = conceptTag,
            displayOrder = displayOrder,
            status = DiagnosticQuestionStatus.ACTIVE,
        )
    }

    companion object {
        fun from(
            diagnosticSessionId: DiagnosticSessionId,
            question: DiagnosticQuestion,
        ): DiagnosticSessionQuestionJpaEntity {
            return DiagnosticSessionQuestionJpaEntity(
                id = snapshotQuestionId(diagnosticSessionId, question.id),
                diagnosticSessionId = diagnosticSessionId.value,
                questionId = question.id.value,
                mathArea = question.mathArea,
                prompt = question.prompt,
                correctChoiceId = question.correctChoiceId.value,
                conceptTag = question.conceptTag,
                displayOrder = question.displayOrder,
            )
        }
    }
}

@Entity
@Table(name = "diagnostic_session_question_choices")
class DiagnosticSessionQuestionChoiceJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 220)
    var id: String = "",

    @Column(name = "diagnostic_session_question_id", nullable = false, length = 160)
    var diagnosticSessionQuestionId: String = "",

    @Column(name = "choice_id", nullable = false, length = 40)
    var choiceId: String = "",

    @Column(name = "text", nullable = false, length = 200)
    var text: String = "",

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,
) {
    fun toDomain(): DiagnosticQuestionChoice {
        return DiagnosticQuestionChoice(
            id = DiagnosticQuestionChoiceId(choiceId),
            text = text,
        )
    }

    companion object {
        fun from(
            diagnosticSessionId: DiagnosticSessionId,
            question: DiagnosticQuestion,
            choice: DiagnosticQuestionChoice,
            displayOrder: Int,
        ): DiagnosticSessionQuestionChoiceJpaEntity {
            val snapshotQuestionId = snapshotQuestionId(diagnosticSessionId, question.id)
            return DiagnosticSessionQuestionChoiceJpaEntity(
                id = "$snapshotQuestionId:${choice.id.value}",
                diagnosticSessionQuestionId = snapshotQuestionId,
                choiceId = choice.id.value,
                text = choice.text,
                displayOrder = displayOrder,
            )
        }
    }
}

private fun snapshotQuestionId(
    diagnosticSessionId: DiagnosticSessionId,
    questionId: DiagnosticQuestionId,
): String {
    return "${diagnosticSessionId.value}:${questionId.value}"
}

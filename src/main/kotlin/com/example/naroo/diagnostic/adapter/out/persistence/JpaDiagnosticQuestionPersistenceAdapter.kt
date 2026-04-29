package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaDiagnosticQuestionPersistenceAdapter(
    private val questionRepository: SpringDataDiagnosticQuestionJpaRepository,
    private val choiceRepository: SpringDataDiagnosticQuestionChoiceJpaRepository,
) : DiagnosticQuestionRepositoryPort {
    override fun findByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return toDomain(questionRepository.findByMathAreaOrderByDisplayOrderAsc(mathArea))
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return toDomain(questionRepository.findAllByOrderByMathAreaAscDisplayOrderAsc())
    }

    private fun toDomain(questions: List<DiagnosticQuestionJpaEntity>): List<DiagnosticQuestion> {
        if (questions.isEmpty()) {
            return emptyList()
        }

        val choicesByQuestionId = choiceRepository
            .findAllByQuestionIdInOrderByDisplayOrderAsc(questions.map { it.id })
            .groupBy { it.questionId }

        return questions.map { question ->
            question.toDomain(choicesByQuestionId.getValue(question.id))
        }
    }
}

interface SpringDataDiagnosticQuestionJpaRepository : JpaRepository<DiagnosticQuestionJpaEntity, String> {
    fun findByMathAreaOrderByDisplayOrderAsc(mathArea: MathArea): List<DiagnosticQuestionJpaEntity>
    fun findAllByOrderByMathAreaAscDisplayOrderAsc(): List<DiagnosticQuestionJpaEntity>
}

interface SpringDataDiagnosticQuestionChoiceJpaRepository : JpaRepository<DiagnosticQuestionChoiceJpaEntity, String> {
    fun findAllByQuestionIdInOrderByDisplayOrderAsc(questionIds: List<String>): List<DiagnosticQuestionChoiceJpaEntity>
}

@Entity
@Table(name = "diagnostic_questions")
class DiagnosticQuestionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 80)
    var id: String = "",

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
    fun toDomain(choices: List<DiagnosticQuestionChoiceJpaEntity>): DiagnosticQuestion {
        return DiagnosticQuestion(
            id = DiagnosticQuestionId(id),
            mathArea = mathArea,
            prompt = prompt,
            choices = choices.map(DiagnosticQuestionChoiceJpaEntity::toDomain),
            correctChoiceId = DiagnosticQuestionChoiceId(correctChoiceId),
            conceptTag = conceptTag,
            displayOrder = displayOrder,
        )
    }
}

@Entity
@Table(name = "diagnostic_question_choices")
class DiagnosticQuestionChoiceJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 140)
    var id: String = "",

    @Column(name = "question_id", nullable = false, length = 80)
    var questionId: String = "",

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
}

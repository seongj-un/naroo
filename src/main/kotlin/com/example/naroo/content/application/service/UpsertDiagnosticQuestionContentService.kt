package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.DiagnosticQuestionChoiceContentResult
import com.example.naroo.content.port.`in`.DiagnosticQuestionContentResult
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionChoiceContentCommand
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionContentCommand
import com.example.naroo.content.port.`in`.UpsertDiagnosticQuestionContentUseCase
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionStatus
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import org.springframework.stereotype.Service

@Service
class UpsertDiagnosticQuestionContentService(
    private val diagnosticQuestionRepositoryPort: DiagnosticQuestionRepositoryPort,
) : UpsertDiagnosticQuestionContentUseCase {
    override fun upsert(command: UpsertDiagnosticQuestionContentCommand): DiagnosticQuestionContentResult {
        val choices = command.choices.toChoices()
        val correctChoiceId = DiagnosticQuestionChoiceId(command.correctChoiceId.trim())
        if (choices.none { it.id == correctChoiceId }) {
            throw IllegalArgumentException("correctChoiceId must exist in choices")
        }

        val question = DiagnosticQuestion(
            id = DiagnosticQuestionId(command.id.trim()),
            mathArea = command.mathArea,
            prompt = command.prompt.trim().also { require(it.isNotBlank()) },
            choices = choices,
            correctChoiceId = correctChoiceId,
            conceptTag = command.conceptTag.trim().also { require(it.isNotBlank()) },
            displayOrder = command.displayOrder.also { require(it > 0) },
            status = DiagnosticQuestionStatus.valueOf(command.status.trim().uppercase()),
        )

        return diagnosticQuestionRepositoryPort.save(question).toResult()
    }

    private fun List<UpsertDiagnosticQuestionChoiceContentCommand>.toChoices(): List<DiagnosticQuestionChoice> {
        require(isNotEmpty()) { "choices must not be empty" }
        val ids = map { it.id.trim() }
        require(ids.distinct().size == ids.size) { "choice ids must be unique" }
        return map { choice ->
            DiagnosticQuestionChoice(
                id = DiagnosticQuestionChoiceId(choice.id.trim()),
                text = choice.text.trim().also { require(it.isNotBlank()) },
            )
        }
    }

    private fun DiagnosticQuestion.toResult(): DiagnosticQuestionContentResult {
        return DiagnosticQuestionContentResult(
            id = id.value,
            mathArea = mathArea,
            prompt = prompt,
            correctChoiceId = correctChoiceId.value,
            conceptTag = conceptTag,
            displayOrder = displayOrder,
            status = status.name,
            choices = choices.map {
                DiagnosticQuestionChoiceContentResult(
                    id = it.id.value,
                    text = it.text,
                )
            },
        )
    }
}

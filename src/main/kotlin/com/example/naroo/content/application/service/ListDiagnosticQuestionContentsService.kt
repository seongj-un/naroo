package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.DiagnosticQuestionChoiceContentResult
import com.example.naroo.content.port.`in`.DiagnosticQuestionContentResult
import com.example.naroo.content.port.`in`.ListDiagnosticQuestionContentsUseCase
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListDiagnosticQuestionContentsService(
    private val diagnosticQuestionRepositoryPort: DiagnosticQuestionRepositoryPort,
) : ListDiagnosticQuestionContentsUseCase {
    override fun list(): List<DiagnosticQuestionContentResult> {
        return diagnosticQuestionRepositoryPort.findAll().map { it.toResult() }
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

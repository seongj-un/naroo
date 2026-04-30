package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionChoiceResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionResult
import com.example.naroo.diagnostic.port.`in`.DiagnosticQuestionsResult
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsUseCase
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class GetDiagnosticQuestionsService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticQuestionRepositoryPort: DiagnosticQuestionRepositoryPort,
    private val clock: Clock,
) : GetDiagnosticQuestionsUseCase {
    override fun get(command: GetDiagnosticQuestionsCommand): DiagnosticQuestionsResult {
        val userId = UserId(command.userId)
        val session = diagnosticSessionRepositoryPort.findById(DiagnosticSessionId(command.diagnosticSessionId))
            ?.takeIf { it.userId == userId }
            ?: throw DiagnosticException.DiagnosticSessionNotFound

        val activeSession = if (session.status == DiagnosticSessionStatus.READY) {
            diagnosticSessionRepositoryPort.save(
                session.copy(
                    status = DiagnosticSessionStatus.IN_PROGRESS,
                    updatedAt = Instant.now(clock),
                ),
            )
        } else {
            session
        }

        val questions = diagnosticQuestionRepositoryPort.findActiveByMathArea(activeSession.mathArea)
        if (questions.isEmpty()) {
            throw DiagnosticException.DiagnosticQuestionsNotFound
        }

        return DiagnosticQuestionsResult(
            diagnosticSessionId = activeSession.id.value,
            mathArea = activeSession.mathArea,
            status = activeSession.status,
            questions = questions.map { question ->
                DiagnosticQuestionResult(
                    id = question.id.value,
                    prompt = question.prompt,
                    choices = question.choices.map { choice ->
                        DiagnosticQuestionChoiceResult(
                            id = choice.id.value,
                            text = choice.text,
                        )
                    },
                )
            },
        )
    }
}

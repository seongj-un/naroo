package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticAnswer
import com.example.naroo.diagnostic.domain.DiagnosticAnswerId
import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswerCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersUseCase
import com.example.naroo.diagnostic.port.`in`.SubmittedDiagnosticResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticAnswerRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SubmitDiagnosticAnswersService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticAnswerRepositoryPort: DiagnosticAnswerRepositoryPort,
    private val diagnosticSessionQuestionSnapshotRepositoryPort: DiagnosticSessionQuestionSnapshotRepositoryPort,
    private val diagnosticResultRepositoryPort: DiagnosticResultRepositoryPort,
    private val clock: Clock,
) : SubmitDiagnosticAnswersUseCase {
    override fun submit(command: SubmitDiagnosticAnswersCommand): SubmittedDiagnosticResult {
        val userId = UserId(command.userId)
        val sessionId = DiagnosticSessionId(command.diagnosticSessionId)
        val session = diagnosticSessionRepositoryPort.findById(sessionId)
            ?.takeIf { it.userId == userId }
            ?: throw DiagnosticException.DiagnosticSessionNotFound

        if (session.status == DiagnosticSessionStatus.COMPLETED ||
            diagnosticAnswerRepositoryPort.existsByDiagnosticSessionId(session.id)
        ) {
            throw DiagnosticException.DiagnosticAlreadyCompleted
        }

        val questions = diagnosticSessionQuestionSnapshotRepositoryPort.findByDiagnosticSessionId(session.id)
        if (questions.isEmpty()) {
            throw DiagnosticException.DiagnosticQuestionsNotFound
        }

        val submittedAnswersByQuestionId = command.answers.associateByQuestionId()
        val questionsById = questions.associateBy { it.id.value }
        if (submittedAnswersByQuestionId.keys != questionsById.keys) {
            throw DiagnosticException.InvalidDiagnosticAnswer
        }

        val now = Instant.now(clock)
        val answers = questions.map { question ->
            val submittedAnswer = submittedAnswersByQuestionId.getValue(question.id.value)
            question.toAnswer(session, submittedAnswer, now)
        }
        diagnosticAnswerRepositoryPort.saveAll(answers)

        val result = answers.toResult(session, now)
        diagnosticResultRepositoryPort.save(result)

        val completedSession = session.copy(
            status = DiagnosticSessionStatus.COMPLETED,
            updatedAt = now,
        )
        val savedSession = diagnosticSessionRepositoryPort.save(completedSession)

        return result.toSubmittedResult(savedSession.status)
    }

    private fun List<SubmitDiagnosticAnswerCommand>.associateByQuestionId(): Map<String, SubmitDiagnosticAnswerCommand> {
        if (isEmpty()) {
            throw DiagnosticException.InvalidDiagnosticAnswer
        }
        val answersByQuestionId = associateBy { it.questionId }
        if (answersByQuestionId.size != size) {
            throw DiagnosticException.InvalidDiagnosticAnswer
        }
        return answersByQuestionId
    }

    private fun DiagnosticQuestion.toAnswer(
        session: DiagnosticSession,
        submittedAnswer: SubmitDiagnosticAnswerCommand,
        answeredAt: Instant,
    ): DiagnosticAnswer {
        val selectedChoiceId = DiagnosticQuestionChoiceId(submittedAnswer.selectedChoiceId)
        if (choices.none { it.id == selectedChoiceId }) {
            throw DiagnosticException.InvalidDiagnosticAnswer
        }

        return DiagnosticAnswer(
            id = DiagnosticAnswerId("${session.id.value}:${id.value}"),
            diagnosticSessionId = session.id,
            userId = session.userId,
            questionId = id,
            selectedChoiceId = selectedChoiceId,
            correctChoiceId = correctChoiceId,
            conceptTag = conceptTag,
            isCorrect = selectedChoiceId == correctChoiceId,
            isUnknown = selectedChoiceId.value == "unknown",
            answeredAt = answeredAt,
        )
    }

    private fun List<DiagnosticAnswer>.toResult(
        session: DiagnosticSession,
        createdAt: Instant,
    ): DiagnosticResult {
        val weakLinks = weakLinks()
        val primaryRecoveryConcept = weakLinks.first()
        return DiagnosticResult(
            diagnosticSessionId = session.id,
            userId = session.userId,
            mathArea = session.mathArea,
            totalQuestionCount = size,
            correctCount = count { it.isCorrect },
            wrongCount = count { !it.isCorrect && !it.isUnknown },
            unknownCount = count { it.isUnknown },
            weakLinks = weakLinks,
            primaryRecoveryConcept = primaryRecoveryConcept,
            summary = "전체가 무너진 게 아니에요. 다음 10분은 $primaryRecoveryConcept 부터 가볍게 다시 시작하면 좋아요.",
            createdAt = createdAt,
        )
    }

    private fun List<DiagnosticAnswer>.weakLinks(): List<String> {
        val missedConcepts = filter { !it.isCorrect }
            .groupBy { it.conceptTag }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, List<DiagnosticAnswer>>> { entry ->
                    entry.value.count { it.isUnknown }
                }.thenByDescending { entry ->
                    entry.value.size
                }.thenBy { it.key },
            )
            .map { it.key }

        return missedConcepts
            .ifEmpty { map { it.conceptTag }.distinct() }
            .take(3)
    }

    private fun DiagnosticResult.toSubmittedResult(status: DiagnosticSessionStatus): SubmittedDiagnosticResult {
        return SubmittedDiagnosticResult(
            diagnosticSessionId = diagnosticSessionId.value,
            mathArea = mathArea,
            status = status,
            totalQuestionCount = totalQuestionCount,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unknownCount = unknownCount,
            weakLinks = weakLinks,
            primaryRecoveryConcept = primaryRecoveryConcept,
            summary = summary,
        )
    }
}

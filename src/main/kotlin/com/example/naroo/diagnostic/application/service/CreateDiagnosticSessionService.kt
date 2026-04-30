package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionUseCase
import com.example.naroo.diagnostic.port.`in`.CreatedDiagnosticSessionResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionIdGeneratorPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionQuestionSnapshotRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class CreateDiagnosticSessionService(
    private val startingPointSelectionRepositoryPort: StartingPointSelectionRepositoryPort,
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticQuestionRepositoryPort: DiagnosticQuestionRepositoryPort,
    private val diagnosticSessionQuestionSnapshotRepositoryPort: DiagnosticSessionQuestionSnapshotRepositoryPort,
    private val diagnosticSessionIdGeneratorPort: DiagnosticSessionIdGeneratorPort,
    private val clock: Clock,
) : CreateDiagnosticSessionUseCase {
    @Transactional
    override fun create(command: CreateDiagnosticSessionCommand): CreatedDiagnosticSessionResult {
        val userId = UserId(command.userId)
        val startingPointSelection = startingPointSelectionRepositoryPort.findByUserId(userId)
            ?: throw DiagnosticException.StartingPointSelectionRequired
        val questions = diagnosticQuestionRepositoryPort.findActiveByMathArea(startingPointSelection.mathArea)
        if (questions.isEmpty()) {
            throw DiagnosticException.DiagnosticQuestionsNotFound
        }
        val now = Instant.now(clock)

        val session = DiagnosticSession(
            id = diagnosticSessionIdGeneratorPort.generate(),
            userId = userId,
            startingPointSelectionId = startingPointSelection.id,
            mathArea = startingPointSelection.mathArea,
            questionSnapshotVersion = 1,
            status = DiagnosticSessionStatus.READY,
            createdAt = now,
            updatedAt = now,
        )

        val savedSession = diagnosticSessionRepositoryPort.save(session)
        diagnosticSessionQuestionSnapshotRepositoryPort.saveSnapshot(savedSession.id, questions)
        return savedSession.toResult()
    }

    private fun DiagnosticSession.toResult(): CreatedDiagnosticSessionResult {
        return CreatedDiagnosticSessionResult(
            id = id.value,
            userId = userId.value,
            startingPointSelectionId = startingPointSelectionId.value,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

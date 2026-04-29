package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionUseCase
import com.example.naroo.diagnostic.port.`in`.CreatedDiagnosticSessionResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionIdGeneratorPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class CreateDiagnosticSessionService(
    private val startingPointSelectionRepositoryPort: StartingPointSelectionRepositoryPort,
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticSessionIdGeneratorPort: DiagnosticSessionIdGeneratorPort,
    private val clock: Clock,
) : CreateDiagnosticSessionUseCase {
    override fun create(command: CreateDiagnosticSessionCommand): CreatedDiagnosticSessionResult {
        val userId = UserId(command.userId)
        val startingPointSelection = startingPointSelectionRepositoryPort.findByUserId(userId)
            ?: throw DiagnosticException.StartingPointSelectionRequired
        val now = Instant.now(clock)

        val session = DiagnosticSession(
            id = diagnosticSessionIdGeneratorPort.generate(),
            userId = userId,
            startingPointSelectionId = startingPointSelection.id,
            mathArea = startingPointSelection.mathArea,
            status = DiagnosticSessionStatus.READY,
            createdAt = now,
            updatedAt = now,
        )

        return diagnosticSessionRepositoryPort.save(session).toResult()
    }

    private fun DiagnosticSession.toResult(): CreatedDiagnosticSessionResult {
        return CreatedDiagnosticSessionResult(
            id = id.value,
            userId = userId.value,
            startingPointSelectionId = startingPointSelectionId.value,
            mathArea = mathArea,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

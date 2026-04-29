package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import java.time.Instant

fun interface CreateDiagnosticSessionUseCase {
    fun create(command: CreateDiagnosticSessionCommand): CreatedDiagnosticSessionResult
}

data class CreateDiagnosticSessionCommand(
    val userId: String,
)

data class CreatedDiagnosticSessionResult(
    val id: String,
    val userId: String,
    val startingPointSelectionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

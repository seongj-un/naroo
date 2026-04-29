package com.example.naroo.diagnostic.domain

import com.example.naroo.user.domain.UserId
import java.time.Instant

data class DiagnosticSession(
    val id: DiagnosticSessionId,
    val userId: UserId,
    val startingPointSelectionId: StartingPointSelectionId,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
value class DiagnosticSessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "diagnosticSessionId must not be blank" }
    }
}

enum class DiagnosticSessionStatus {
    READY,
    IN_PROGRESS,
    COMPLETED,
}

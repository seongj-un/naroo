package com.example.naroo.diagnostic.domain

import com.example.naroo.user.domain.UserId
import java.time.Instant

data class DiagnosticResult(
    val diagnosticSessionId: DiagnosticSessionId,
    val userId: UserId,
    val mathArea: MathArea,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
    val createdAt: Instant,
)

package com.example.naroo.diagnostic.domain

import com.example.naroo.user.domain.UserId
import java.time.Instant

data class DiagnosticAnswer(
    val id: DiagnosticAnswerId,
    val diagnosticSessionId: DiagnosticSessionId,
    val userId: UserId,
    val questionId: DiagnosticQuestionId,
    val selectedChoiceId: DiagnosticQuestionChoiceId,
    val correctChoiceId: DiagnosticQuestionChoiceId,
    val conceptTag: String,
    val isCorrect: Boolean,
    val isUnknown: Boolean,
    val answeredAt: Instant,
)

@JvmInline
value class DiagnosticAnswerId(val value: String) {
    init {
        require(value.isNotBlank()) { "diagnosticAnswerId must not be blank" }
    }
}

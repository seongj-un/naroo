package com.example.naroo.auth.port.`in`

import java.time.Instant

fun interface ResendEmailVerificationUseCase {
    fun resend(command: ResendEmailVerificationCommand): ResentEmailVerificationResult
}

data class ResendEmailVerificationCommand(
    val userId: String,
)

data class ResentEmailVerificationResult(
    val userId: String,
    val email: String,
    val emailVerified: Boolean,
    val nextRetryAt: Instant,
)

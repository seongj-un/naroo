package com.example.naroo.auth.port.`in`

fun interface VerifyEmailUseCase {
    fun verify(command: VerifyEmailCommand): VerifiedEmailResult
}

data class VerifyEmailCommand(
    val token: String,
)

data class VerifiedEmailResult(
    val userId: String,
    val email: String,
    val emailVerified: Boolean,
)

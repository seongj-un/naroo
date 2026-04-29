package com.example.naroo.auth.port.`out`

fun interface EmailSenderPort {
    fun sendEmailVerification(message: EmailVerificationMessage)
}

data class EmailVerificationMessage(
    val userId: String,
    val email: String,
    val token: String,
)

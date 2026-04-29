package com.example.naroo.auth.port.`out`

import java.time.Instant

interface EmailVerificationTokenPort {
    fun issue(userId: String, email: String): IssuedEmailVerificationToken
    fun hash(rawToken: String): String
}

data class IssuedEmailVerificationToken(
    val id: String,
    val value: String,
    val tokenHash: String,
    val expiresAt: Instant,
)

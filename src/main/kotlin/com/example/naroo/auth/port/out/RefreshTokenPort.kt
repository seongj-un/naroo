package com.example.naroo.auth.port.`out`

import java.time.Instant

interface RefreshTokenPort {
    fun issue(userId: String): IssuedRefreshToken
    fun hash(rawToken: String): String
}

data class IssuedRefreshToken(
    val id: String,
    val value: String,
    val tokenHash: String,
    val expiresAt: Instant,
)

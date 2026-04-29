package com.example.naroo.auth.port.`in`

import java.time.Instant

fun interface ReissueTokenUseCase {
    fun reissue(command: ReissueTokenCommand): ReissuedTokenResult
}

data class ReissueTokenCommand(
    val refreshToken: String,
)

data class ReissuedTokenResult(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
    val refreshToken: String,
    val refreshTokenExpiresAt: Instant,
)

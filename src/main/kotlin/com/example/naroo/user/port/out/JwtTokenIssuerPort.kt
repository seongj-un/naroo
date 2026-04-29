package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.UserAccount
import java.time.Instant

fun interface JwtTokenIssuerPort {
    fun issue(userAccount: UserAccount): IssuedJwtToken
}

data class IssuedJwtToken(
    val value: String,
    val expiresAt: Instant,
)

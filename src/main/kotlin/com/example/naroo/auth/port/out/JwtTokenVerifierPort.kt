package com.example.naroo.auth.port.`out`

fun interface JwtTokenVerifierPort {
    fun verify(token: String): VerifiedJwtToken?
}

data class VerifiedJwtToken(
    val tokenId: String,
    val userId: String,
    val loginId: String,
    val emailVerified: Boolean,
    val nickname: String,
)

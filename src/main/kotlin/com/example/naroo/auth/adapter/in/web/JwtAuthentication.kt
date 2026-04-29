package com.example.naroo.auth.adapter.`in`.web

data class JwtAuthentication(
    val tokenId: String,
    val userId: String,
    val loginId: String,
    val emailVerified: Boolean,
    val nickname: String,
) {
    companion object {
        const val REQUEST_ATTRIBUTE = "naroo.jwtAuthentication"
    }
}

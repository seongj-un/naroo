package com.example.naroo.auth.adapter.`in`.web

data class JwtAuthentication(
    val tokenId: String,
    val userId: String,
    val loginId: String,
    val emailVerified: Boolean,
    val nickname: String,
) {
    companion object {
        fun current(): JwtAuthentication? {
            val authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .authentication
            return authentication?.principal as? JwtAuthentication
        }
    }
}

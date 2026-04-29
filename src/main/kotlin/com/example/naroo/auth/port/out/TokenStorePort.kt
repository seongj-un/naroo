package com.example.naroo.auth.port.`out`

import java.time.Instant

interface TokenStorePort {
    fun save(token: StoredToken)
    fun findUserIdByTokenId(tokenId: String): String?
}

data class StoredToken(
    val tokenId: String,
    val userId: String,
    val expiresAt: Instant,
)

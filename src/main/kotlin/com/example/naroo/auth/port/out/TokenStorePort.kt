package com.example.naroo.auth.port.`out`

import java.time.Instant

fun interface TokenStorePort {
    fun save(token: StoredToken)
}

data class StoredToken(
    val tokenId: String,
    val userId: String,
    val expiresAt: Instant,
)

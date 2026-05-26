package com.example.naroo.auth.port.`out`

import java.time.Instant

interface TokenStorePort {
    fun saveAccessToken(token: StoredAccessToken)
    fun findUserIdByAccessTokenId(tokenId: String): String?
    fun saveRefreshToken(token: StoredRefreshToken)
    fun consumeRefreshToken(tokenId: String): StoredRefreshToken?
    fun saveEmailVerificationToken(token: StoredEmailVerificationToken)
    fun findEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return null
    }
    fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken?

    fun deleteEmailVerificationToken(tokenId: String) {
    }

    fun saveEmailVerificationResendCooldown(
        userId: String,
        availableAt: Instant,
    ) {
    }

    fun findEmailVerificationResendAvailableAt(userId: String): Instant? {
        return null
    }
}

data class StoredAccessToken(
    val tokenId: String,
    val userId: String,
    val expiresAt: Instant,
)

data class StoredRefreshToken(
    val tokenId: String,
    val userId: String,
    val tokenHash: String,
    val expiresAt: Instant,
)

data class StoredEmailVerificationToken(
    val tokenId: String,
    val userId: String,
    val email: String,
    val tokenHash: String,
    val expiresAt: Instant,
)

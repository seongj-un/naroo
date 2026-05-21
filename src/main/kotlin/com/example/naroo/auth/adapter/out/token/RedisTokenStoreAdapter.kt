package com.example.naroo.auth.adapter.`out`.token

import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Component
class RedisTokenStoreAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val clock: Clock,
) : TokenStorePort {
    override fun saveAccessToken(token: StoredAccessToken) {
        val ttl = Duration.between(clock.instant(), token.expiresAt)
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(
            accessTokenKey(token.tokenId),
            token.userId,
            ttl,
        )
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        return redisTemplate.opsForValue().get(accessTokenKey(tokenId))
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        val ttl = Duration.between(clock.instant(), token.expiresAt)
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(
            refreshTokenKey(token.tokenId),
            "${token.userId}:${token.tokenHash}",
            ttl,
        )
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        val key = refreshTokenKey(tokenId)
        val storedValue = redisTemplate.opsForValue().getAndDelete(key) ?: return null

        val separatorIndex = storedValue.indexOf(':')
        if (separatorIndex <= 0 || separatorIndex == storedValue.lastIndex) {
            return null
        }

        return StoredRefreshToken(
            tokenId = tokenId,
            userId = storedValue.substring(0, separatorIndex),
            tokenHash = storedValue.substring(separatorIndex + 1),
            expiresAt = clock.instant(),
        )
    }

    override fun saveEmailVerificationToken(token: StoredEmailVerificationToken) {
        val ttl = Duration.between(clock.instant(), token.expiresAt)
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(
            emailVerificationTokenKey(token.tokenId),
            listOf(token.userId, token.email, token.tokenHash).joinToString("\n"),
            ttl,
        )
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        val key = emailVerificationTokenKey(tokenId)
        val storedValue = redisTemplate.opsForValue().getAndDelete(key) ?: return null
        val parts = storedValue.split("\n")
        if (parts.size != 3 || parts.any { it.isBlank() }) {
            return null
        }

        return StoredEmailVerificationToken(
            tokenId = tokenId,
            userId = parts[0],
            email = parts[1],
            tokenHash = parts[2],
            expiresAt = clock.instant(),
        )
    }

    override fun deleteEmailVerificationToken(tokenId: String) {
        redisTemplate.delete(emailVerificationTokenKey(tokenId))
    }

    override fun saveEmailVerificationResendCooldown(userId: String, availableAt: Instant) {
        val ttl = Duration.between(clock.instant(), availableAt)
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(
            emailVerificationResendCooldownKey(userId),
            availableAt.toString(),
            ttl,
        )
    }

    override fun findEmailVerificationResendAvailableAt(userId: String): Instant? {
        val storedValue = redisTemplate.opsForValue().get(emailVerificationResendCooldownKey(userId)) ?: return null
        return runCatching { Instant.parse(storedValue) }.getOrNull()
    }

    private fun accessTokenKey(tokenId: String): String {
        return "auth:access-token:$tokenId"
    }

    private fun refreshTokenKey(tokenId: String): String {
        return "auth:refresh-token:$tokenId"
    }

    private fun emailVerificationTokenKey(tokenId: String): String {
        return "auth:email-verification-token:$tokenId"
    }

    private fun emailVerificationResendCooldownKey(userId: String): String {
        return "auth:email-verification-resend-cooldown:$userId"
    }
}

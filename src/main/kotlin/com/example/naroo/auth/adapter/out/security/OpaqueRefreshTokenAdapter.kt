package com.example.naroo.auth.adapter.`out`.security

import com.example.naroo.auth.port.`out`.IssuedRefreshToken
import com.example.naroo.auth.port.`out`.RefreshTokenPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.util.Base64
import java.util.UUID

@Component
class OpaqueRefreshTokenAdapter(
    @Value("\${naroo.jwt.refresh-token-ttl-days:3}") private val refreshTokenTtlDays: Long,
    private val clock: Clock,
) : RefreshTokenPort {
    private val random = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    override fun issue(userId: String): IssuedRefreshToken {
        val tokenId = UUID.randomUUID().toString()
        val secret = ByteArray(48)
        random.nextBytes(secret)
        val rawToken = "$tokenId.${encoder.encodeToString(secret)}"

        return IssuedRefreshToken(
            id = tokenId,
            value = rawToken,
            tokenHash = hash(rawToken),
            expiresAt = clock.instant().plus(Duration.ofDays(refreshTokenTtlDays)),
        )
    }

    override fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray())
        return encoder.encodeToString(digest)
    }
}

package com.example.naroo.auth.adapter.`out`.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.example.naroo.auth.port.`out`.IssuedJwtToken
import com.example.naroo.auth.port.`out`.JwtTokenIssuerPort
import com.example.naroo.auth.port.`out`.JwtTokenVerifierPort
import com.example.naroo.auth.port.`out`.VerifiedJwtToken
import com.example.naroo.user.domain.UserAccount
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HmacJwtTokenIssuerAdapter(
    @Value("\${naroo.jwt.secret}") private val secret: String,
    @Value("\${naroo.jwt.access-token-ttl-minutes:60}") private val accessTokenTtlMinutes: Long,
    private val clock: Clock,
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : JwtTokenIssuerPort, JwtTokenVerifierPort {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private val mapType = object : TypeReference<Map<String, Any>>() {}

    override fun issue(userAccount: UserAccount): IssuedJwtToken {
        require(secret.toByteArray(StandardCharsets.UTF_8).size >= 32) {
            "naroo.jwt.secret must be at least 32 bytes"
        }

        val issuedAt = Instant.now(clock)
        val expiresAt = issuedAt.plus(Duration.ofMinutes(accessTokenTtlMinutes))
        val tokenId = UUID.randomUUID().toString()
        val header = objectMapper.writeValueAsString(mapOf("alg" to "HS256", "typ" to "JWT"))
        val payload = buildPayload(userAccount, tokenId, issuedAt, expiresAt)
        val unsignedToken = "${base64Url(header)}.${base64Url(payload)}"
        val signature = sign(unsignedToken)

        return IssuedJwtToken(
            id = tokenId,
            value = "$unsignedToken.$signature",
            expiresAt = expiresAt,
        )
    }

    override fun verify(token: String): VerifiedJwtToken? {
        if (secret.toByteArray(StandardCharsets.UTF_8).size < 32) {
            return null
        }

        val parts = token.split(".")
        if (parts.size != 3) {
            return null
        }

        val unsignedToken = "${parts[0]}.${parts[1]}"
        val expectedSignature = sign(unsignedToken)
        if (!MessageDigest.isEqual(expectedSignature.toByteArray(), parts[2].toByteArray())) {
            return null
        }

        val payload = decodePayload(parts[1]) ?: return null
        val expiresAt = payload["exp"].asLongOrNull() ?: return null
        if (Instant.now(clock).epochSecond >= expiresAt) {
            return null
        }

        return VerifiedJwtToken(
            tokenId = payload["jti"] as? String ?: return null,
            userId = payload["sub"] as? String ?: return null,
            loginId = payload["loginId"] as? String ?: return null,
            nickname = payload["nickname"] as? String ?: return null,
        )
    }

    private fun buildPayload(userAccount: UserAccount, tokenId: String, issuedAt: Instant, expiresAt: Instant): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "jti" to tokenId,
                "sub" to userAccount.id.value,
                "loginId" to userAccount.loginId.value,
                "nickname" to userAccount.nickname.value,
                "iat" to issuedAt.epochSecond,
                "exp" to expiresAt.epochSecond,
            ),
        )
    }

    private fun base64Url(value: String): String {
        return encoder.encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun sign(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(key)
        return encoder.encodeToString(mac.doFinal(value.toByteArray(StandardCharsets.UTF_8)))
    }

    private fun decodePayload(payload: String): Map<String, Any>? {
        return runCatching {
            val decoded = String(decoder.decode(payload), StandardCharsets.UTF_8)
            objectMapper.readValue(decoded, mapType)
        }.getOrNull()
    }

    private fun Any?.asLongOrNull(): Long? {
        return when (this) {
            is Number -> toLong()
            is String -> toLongOrNull()
            else -> null
        }
    }
}

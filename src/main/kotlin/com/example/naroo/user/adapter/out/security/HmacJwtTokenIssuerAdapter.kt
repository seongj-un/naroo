package com.example.naroo.user.adapter.`out`.security

import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.port.`out`.IssuedJwtToken
import com.example.naroo.user.port.`out`.JwtTokenIssuerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HmacJwtTokenIssuerAdapter(
    @Value("\${naroo.jwt.secret}") private val secret: String,
    @Value("\${naroo.jwt.access-token-ttl-minutes:60}") private val accessTokenTtlMinutes: Long,
    private val clock: Clock,
) : JwtTokenIssuerPort {
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    override fun issue(userAccount: UserAccount): IssuedJwtToken {
        require(secret.toByteArray(StandardCharsets.UTF_8).size >= 32) {
            "naroo.jwt.secret must be at least 32 bytes"
        }

        val issuedAt = Instant.now(clock)
        val expiresAt = issuedAt.plus(Duration.ofMinutes(accessTokenTtlMinutes))
        val header = """{"alg":"HS256","typ":"JWT"}"""
        val payload = buildPayload(userAccount, issuedAt, expiresAt)
        val unsignedToken = "${base64Url(header)}.${base64Url(payload)}"
        val signature = sign(unsignedToken)

        return IssuedJwtToken(
            value = "$unsignedToken.$signature",
            expiresAt = expiresAt,
        )
    }

    private fun buildPayload(userAccount: UserAccount, issuedAt: Instant, expiresAt: Instant): String {
        return """
            {
              "sub":"${jsonEscape(userAccount.id.value)}",
              "loginId":"${jsonEscape(userAccount.loginId.value)}",
              "nickname":"${jsonEscape(userAccount.nickname.value)}",
              "iat":${issuedAt.epochSecond},
              "exp":${expiresAt.epochSecond}
            }
        """.trimIndent().replace("\n", "").replace("  ", "")
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

    private fun jsonEscape(value: String): String {
        return buildString {
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    else -> append(character)
                }
            }
        }
    }
}

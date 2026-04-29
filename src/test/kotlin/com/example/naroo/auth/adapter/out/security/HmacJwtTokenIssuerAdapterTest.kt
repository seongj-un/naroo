package com.example.naroo.auth.adapter.`out`.security

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HmacJwtTokenIssuerAdapterTest {
    @Test
    fun `issues and verifies hmac signed jwt`() {
        val issuer = HmacJwtTokenIssuerAdapter(
            secret = "test-jwt-secret-with-at-least-32-bytes",
            accessTokenTtlMinutes = 30,
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )
        val userAccount = UserAccount(
            id = UserId("user-1"),
            loginId = LoginId.from("student01"),
            passwordHash = PasswordHash("hashed-password"),
            nickname = Nickname.from("나루"),
            mathStatus = MathStatus.UNKNOWN,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
        )

        val token = issuer.issue(userAccount)

        assertEquals(Instant.parse("2026-04-29T00:30:00Z"), token.expiresAt)
        assertTrue(token.id.isNotBlank())
        assertEquals(3, token.value.split(".").size)
        assertTrue(token.value.startsWith("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."))

        val verifiedToken = issuer.verify(token.value)
        assertEquals(token.id, verifiedToken?.tokenId)
        assertEquals("user-1", verifiedToken?.userId)
        assertEquals("student01", verifiedToken?.loginId)
        assertEquals("나루", verifiedToken?.nickname)
    }

    @Test
    fun `rejects expired jwt`() {
        val issuer = HmacJwtTokenIssuerAdapter(
            secret = "test-jwt-secret-with-at-least-32-bytes",
            accessTokenTtlMinutes = -1,
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )
        val userAccount = UserAccount(
            id = UserId("user-1"),
            loginId = LoginId.from("student01"),
            passwordHash = PasswordHash("hashed-password"),
            nickname = Nickname.from("나루"),
            mathStatus = MathStatus.UNKNOWN,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
        )

        val token = issuer.issue(userAccount)

        assertEquals(null, issuer.verify(token.value))
    }
}

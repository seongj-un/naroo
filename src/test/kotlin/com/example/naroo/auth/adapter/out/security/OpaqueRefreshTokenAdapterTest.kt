package com.example.naroo.auth.adapter.`out`.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OpaqueRefreshTokenAdapterTest {
    @Test
    fun `issues opaque refresh token that expires in three days`() {
        val adapter = OpaqueRefreshTokenAdapter(
            refreshTokenTtlDays = 3,
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )

        val token = adapter.issue("user-1")

        assertTrue(token.value.startsWith("${token.id}."))
        assertEquals(Instant.parse("2026-05-02T00:00:00Z"), token.expiresAt)
        assertEquals(adapter.hash(token.value), token.tokenHash)
        assertNotEquals(token.value, token.tokenHash)
    }
}

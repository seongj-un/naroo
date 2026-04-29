package com.example.naroo.auth.adapter.`out`.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OpaqueEmailVerificationTokenAdapterTest {
    @Test
    fun `issues opaque email verification token with configured ttl`() {
        val adapter = OpaqueEmailVerificationTokenAdapter(
            tokenTtlMinutes = 30,
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )

        val token = adapter.issue("user-1", "student01@example.com")

        assertTrue(token.id.isNotBlank())
        assertTrue(token.value.startsWith("${token.id}."))
        assertEquals(Instant.parse("2026-04-29T00:30:00Z"), token.expiresAt)
        assertEquals(adapter.hash(token.value), token.tokenHash)
        assertNotEquals(token.value, token.tokenHash)
    }
}

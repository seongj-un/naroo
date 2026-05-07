package com.example.naroo.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProductionDeploymentGuardTest {
    @Test
    fun `accepts production safe settings`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "production-secret-that-is-long-enough-123",
            refreshCookieSecure = true,
            refreshCookieSameSite = "None",
            allowedOriginsValue = "https://naroo.app, https://www.naroo.app",
        )

        assertDoesNotThrow { guard.validate() }
    }

    @Test
    fun `rejects local jwt secret in prod`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "naroo-local-development-secret-32bytes",
            refreshCookieSecure = true,
            refreshCookieSameSite = "Strict",
            allowedOriginsValue = "https://naroo.app",
        )

        assertThrows(IllegalArgumentException::class.java) {
            guard.validate()
        }
    }

    @Test
    fun `rejects localhost cors origin in prod`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "production-secret-that-is-long-enough-123",
            refreshCookieSecure = true,
            refreshCookieSameSite = "Strict",
            allowedOriginsValue = "http://localhost:3000",
        )

        assertThrows(IllegalArgumentException::class.java) {
            guard.validate()
        }
    }
}

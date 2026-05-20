package com.example.naroo.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProductionDeploymentGuardTest {
    @Test
    fun `accepts production safe settings`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "production-secret-that-is-long-enough-123",
            emailMode = "smtp",
            emailFromAddress = "no-reply@naroo.app",
            verificationUrlTemplate = "https://naroo.app/verify-email?token={token}",
            refreshCookieSecure = true,
            refreshCookieSameSite = "None",
            allowedOriginsValue = "https://naroo.app, https://www.naroo.app",
            mailHost = "smtp.mailgun.org",
        )

        assertDoesNotThrow { guard.validate() }
    }

    @Test
    fun `rejects local jwt secret in prod`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "naroo-local-development-secret-32bytes",
            emailMode = "smtp",
            emailFromAddress = "no-reply@naroo.app",
            verificationUrlTemplate = "https://naroo.app/verify-email?token={token}",
            refreshCookieSecure = true,
            refreshCookieSameSite = "Strict",
            allowedOriginsValue = "https://naroo.app",
            mailHost = "smtp.mailgun.org",
        )

        assertThrows(IllegalArgumentException::class.java) {
            guard.validate()
        }
    }

    @Test
    fun `rejects localhost cors origin in prod`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "production-secret-that-is-long-enough-123",
            emailMode = "smtp",
            emailFromAddress = "no-reply@naroo.app",
            verificationUrlTemplate = "https://naroo.app/verify-email?token={token}",
            refreshCookieSecure = true,
            refreshCookieSameSite = "Strict",
            allowedOriginsValue = "http://localhost:3000",
            mailHost = "smtp.mailgun.org",
        )

        assertThrows(IllegalArgumentException::class.java) {
            guard.validate()
        }
    }

    @Test
    fun `rejects log email mode in prod`() {
        val guard = ProductionDeploymentGuard(
            jwtSecret = "production-secret-that-is-long-enough-123",
            emailMode = "log",
            emailFromAddress = "no-reply@naroo.app",
            verificationUrlTemplate = "https://naroo.app/verify-email?token={token}",
            refreshCookieSecure = true,
            refreshCookieSameSite = "Strict",
            allowedOriginsValue = "https://naroo.app",
            mailHost = "smtp.mailgun.org",
        )

        assertThrows(IllegalArgumentException::class.java) {
            guard.validate()
        }
    }
}

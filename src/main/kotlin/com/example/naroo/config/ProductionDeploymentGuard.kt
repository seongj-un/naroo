package com.example.naroo.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
@Profile("prod")
class ProductionDeploymentGuard(
    @Value("\${naroo.jwt.secret}") private val jwtSecret: String,
    @Value("\${naroo.auth.email.mode:log}") private val emailMode: String,
    @Value("\${naroo.auth.email.from-address:}") private val emailFromAddress: String,
    @Value("\${naroo.auth.email.verification-url-template:}") private val verificationUrlTemplate: String,
    @Value("\${naroo.auth.email.resend.api-key:}") private val resendApiKey: String,
    @Value("\${naroo.auth.refresh-cookie-secure:true}") private val refreshCookieSecure: Boolean,
    @Value("\${naroo.auth.refresh-cookie-same-site:Strict}") private val refreshCookieSameSite: String,
    @Value("\${naroo.cors.allowed-origins:}") private val allowedOriginsValue: String,
    @Value("\${spring.mail.host:}") private val mailHost: String,
) {
    @PostConstruct
    fun validate() {
        require(jwtSecret != DEFAULT_LOCAL_JWT_SECRET) {
            "NAROO_JWT_SECRET must be replaced before starting with SPRING_PROFILES_ACTIVE=prod"
        }
        require(jwtSecret.toByteArray(StandardCharsets.UTF_8).size >= 32) {
            "NAROO_JWT_SECRET must be at least 32 bytes"
        }
        val normalizedEmailMode = normalizeEmailMode(emailMode)
        require(normalizedEmailMode in allowedEmailModes) {
            "naroo.auth.email.mode must be one of smtp or resend in prod"
        }
        when (normalizedEmailMode) {
            "smtp" -> require(mailHost.isNotBlank()) {
                "spring.mail.host must be configured in prod when naroo.auth.email.mode=smtp"
            }

            "resend" -> require(resendApiKey.isNotBlank()) {
                "naroo.auth.email.resend.api-key must be configured in prod when naroo.auth.email.mode=resend"
            }
        }
        require(emailFromAddress.isNotBlank()) {
            "naroo.auth.email.from-address must be configured in prod"
        }
        require(verificationUrlTemplate.startsWith("https://") && verificationUrlTemplate.contains("{token}")) {
            "naroo.auth.email.verification-url-template must be an https URL containing {token} in prod"
        }
        require(refreshCookieSecure) {
            "NAROO_AUTH_REFRESH_COOKIE_SECURE must stay true in prod"
        }
        require(normalizeSameSite(refreshCookieSameSite) in allowedSameSiteValues) {
            "NAROO_AUTH_REFRESH_COOKIE_SAME_SITE must be one of Strict, Lax, None"
        }

        val allowedOrigins = allowedOriginsValue
            .split(",")
            .map(String::trim)
            .filter(String::isNotBlank)

        require(allowedOrigins.isNotEmpty()) {
            "NAROO_CORS_ALLOWED_ORIGINS must contain at least one production frontend origin"
        }
        require(allowedOrigins.none { localhostPattern.containsMatchIn(it) }) {
            "NAROO_CORS_ALLOWED_ORIGINS must not contain localhost or 127.0.0.1 in prod"
        }
        require(allowedOrigins.all { it.startsWith("https://") }) {
            "NAROO_CORS_ALLOWED_ORIGINS must use https origins in prod"
        }
    }

    private fun normalizeSameSite(value: String): String {
        return value.trim().lowercase().replaceFirstChar(Char::titlecase)
    }

    private fun normalizeEmailMode(value: String): String {
        return value.trim().lowercase()
    }

    companion object {
        private const val DEFAULT_LOCAL_JWT_SECRET = "naroo-local-development-secret-32bytes"
        private val localhostPattern = Regex("localhost|127\\.0\\.0\\.1")
        private val allowedSameSiteValues = setOf("Strict", "Lax", "None")
        private val allowedEmailModes = setOf("smtp", "resend")
    }
}

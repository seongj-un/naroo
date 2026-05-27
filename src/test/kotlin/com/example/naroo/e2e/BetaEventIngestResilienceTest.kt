package com.example.naroo.e2e

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "naroo.beta-ops.mode=shadow",
    ],
)
class BetaEventIngestResilienceTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val emailSender: ResilienceCapturingEmailSender,
    @LocalServerPort private val port: Int,
) {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        jdbcTemplate.update("delete from beta_question_heatmap_rows")
        jdbcTemplate.update("delete from beta_funnel_rows")
        jdbcTemplate.update("delete from beta_events")
        jdbcTemplate.update("delete from recovery_mission_submissions")
        jdbcTemplate.update("delete from recovery_missions")
        jdbcTemplate.update("delete from diagnostic_results")
        jdbcTemplate.update("delete from diagnostic_answers")
        jdbcTemplate.update("delete from diagnostic_session_question_choices")
        jdbcTemplate.update("delete from diagnostic_session_questions")
        jdbcTemplate.update("delete from diagnostic_sessions")
        jdbcTemplate.update("delete from diagnostic_starting_points")
        jdbcTemplate.update("delete from user_accounts")
        emailSender.clear()
    }

    @Test
    fun `login still succeeds when beta event ingest fails`() {
        val loginId = "resilience_user"
        val password = "Password123!"

        val signUp = post(
            path = "/api/auth/sign-up",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
                "email" to "resilience@example.com",
                "nickname" to "회복력",
                "mathStatus" to "UNKNOWN",
            ),
        )
        assertEquals(HttpStatus.CREATED.value(), signUp.statusCode)

        val verification = post(
            path = "/api/auth/email/verify",
            body = mapOf("token" to emailSender.latestTokenFor("resilience@example.com")),
        )
        assertEquals(HttpStatus.OK.value(), verification.statusCode)

        val login = post(
            path = "/api/auth/login",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
            ),
        )
        assertEquals(HttpStatus.OK.value(), login.statusCode)
        assertEquals("STUDENT", login.body.dataMap().map("user").string("role"))
    }

    private fun post(path: String, body: Any): ResilienceResponse {
        return exchange(path, HttpMethod.POST, body)
    }

    private fun exchange(path: String, method: HttpMethod, body: Any?): ResilienceResponse {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port$path"))
            .header("Content-Type", "application/json")
        when (method) {
            HttpMethod.POST -> requestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body ?: emptyMap<String, Any>())),
            )

            else -> error("unsupported method: $method")
        }

        val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        return ResilienceResponse(
            statusCode = response.statusCode(),
            body = if (response.body().isBlank()) {
                emptyMap<Any?, Any?>()
            } else {
                objectMapper.readValue(response.body(), Map::class.java)
            },
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<*, *>.dataMap(): Map<String, Any?> {
        return this["data"] as Map<String, Any?>
    }

    private fun Map<String, Any?>.map(key: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return this[key] as Map<String, Any?>
    }

    private fun Map<String, Any?>.string(key: String): String {
        return this[key] as String
    }

    @TestConfiguration
    class ResilienceConfiguration {
        @Bean
        @Primary
        fun resilienceTokenStorePort(): TokenStorePort {
            return ResilienceInMemoryTokenStore()
        }

        @Bean
        @Primary
        fun resilienceEmailSenderPort(): ResilienceCapturingEmailSender {
            return ResilienceCapturingEmailSender()
        }

        @Bean
        @Primary
        fun failingBetaEventRepositoryPort(): BetaEventRepositoryPort {
            return BetaEventRepositoryPort {
                throw IllegalStateException("beta event ingest forced failure")
            }
        }
    }
}

private data class ResilienceResponse(
    val statusCode: Int,
    val body: Map<*, *>,
)

class ResilienceCapturingEmailSender : EmailSenderPort {
    private val messages = ConcurrentHashMap<String, EmailVerificationMessage>()

    override fun sendEmailVerification(message: EmailVerificationMessage) {
        messages[message.email] = message
    }

    fun latestTokenFor(email: String): String {
        return messages[email]?.token ?: error("email verification token not captured for $email")
    }

    fun clear() {
        messages.clear()
    }
}

private class ResilienceInMemoryTokenStore : TokenStorePort {
    private val accessTokens = ConcurrentHashMap<String, String>()
    private val refreshTokens = ConcurrentHashMap<String, StoredRefreshToken>()
    private val emailVerificationTokens = ConcurrentHashMap<String, StoredEmailVerificationToken>()

    override fun saveAccessToken(token: StoredAccessToken) {
        accessTokens[token.tokenId] = token.userId
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        return accessTokens[tokenId]
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        refreshTokens[token.tokenId] = token
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        return refreshTokens.remove(tokenId)
    }

    override fun saveEmailVerificationToken(token: StoredEmailVerificationToken) {
        emailVerificationTokens[token.tokenId] = token
    }

    override fun findEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return emailVerificationTokens[tokenId]
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return emailVerificationTokens.remove(tokenId)
    }

    override fun deleteEmailVerificationToken(tokenId: String) {
        emailVerificationTokens.remove(tokenId)
    }
}

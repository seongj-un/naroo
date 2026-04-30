package com.example.naroo.e2e

import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserLearningFlowSmokeTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @LocalServerPort private val port: Int,
) {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        jdbcTemplate.update("delete from recovery_mission_submissions")
        jdbcTemplate.update("delete from recovery_missions")
        jdbcTemplate.update("delete from diagnostic_results")
        jdbcTemplate.update("delete from diagnostic_answers")
        jdbcTemplate.update("delete from diagnostic_session_question_choices")
        jdbcTemplate.update("delete from diagnostic_session_questions")
        jdbcTemplate.update("delete from diagnostic_sessions")
        jdbcTemplate.update("delete from diagnostic_starting_points")
        jdbcTemplate.update("delete from user_accounts")
    }

    @Test
    fun `sign up diagnose submit recovery mission and open learning home`() {
        val loginId = "smoke_user"
        val password = "Password123!"

        val signUp = post(
            path = "/api/auth/sign-up",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
                "email" to "smoke@example.com",
                "nickname" to "스모크",
                "mathStatus" to "UNKNOWN",
            ),
        )
        assertEquals(HttpStatus.CREATED.value(), signUp.statusCode)

        jdbcTemplate.update("update user_accounts set email_verified = true where login_id = ?", loginId)

        val login = post(
            path = "/api/auth/login",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
            ),
        )
        assertEquals(HttpStatus.OK.value(), login.statusCode)
        val accessToken = login.body.dataMap().string("accessToken")
        assertTrue(accessToken.isNotBlank())

        val startingPoint = post(
            path = "/api/diagnostics/starting-point",
            body = mapOf(
                "selectionType" to "WEAK_AREA",
                "mathArea" to "FUNCTION",
                "note" to "함수가 어려워요",
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), startingPoint.statusCode)

        val diagnosticSession = post(
            path = "/api/diagnostics",
            body = emptyMap<String, Any>(),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), diagnosticSession.statusCode)
        val diagnosticSessionId = diagnosticSession.body.dataMap().string("id")

        val questions = exchange(
            path = "/api/diagnostics/$diagnosticSessionId/questions",
            method = HttpMethod.GET,
            body = null,
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.OK.value(), questions.statusCode)
        assertEquals(2, questions.body.dataMap().list("questions").size)

        val submittedAnswers = post(
            path = "/api/diagnostics/$diagnosticSessionId/answers",
            body = mapOf(
                "answers" to listOf(
                    mapOf("questionId" to "function-substitution-1", "selectedChoiceId" to "b"),
                    mapOf("questionId" to "function-slope-1", "selectedChoiceId" to "unknown"),
                ),
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.OK.value(), submittedAnswers.statusCode)
        assertEquals("linear_function_slope", submittedAnswers.body.dataMap().string("primaryRecoveryConcept"))

        val result = exchange(
            path = "/api/diagnostics/$diagnosticSessionId/result",
            method = HttpMethod.GET,
            body = null,
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.OK.value(), result.statusCode)
        assertNotNull(result.body.dataMap()["nextMissionPreview"])

        val mission = post(
            path = "/api/recovery-missions",
            body = mapOf("diagnosticSessionId" to diagnosticSessionId),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), mission.statusCode)
        val missionId = mission.body.dataMap().string("id")

        val submission = post(
            path = "/api/recovery-missions/$missionId/submissions",
            body = mapOf("answerText" to "x 앞에 붙은 숫자를 보고 기울기가 -3이라는 것을 확인했습니다."),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), submission.statusCode)
        assertEquals("COMPLETED", submission.body.dataMap().map("mission").string("status"))

        val home = exchange(
            path = "/api/me/learning-home",
            method = HttpMethod.GET,
            body = null,
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.OK.value(), home.statusCode)
        assertEquals("CREATE_RECOVERY_MISSION", home.body.dataMap().string("nextAction"))
        assertEquals(1, home.body.dataMap().map("progress").number("completedMissionCount").toInt())
    }

    @Test
    fun `content write APIs require admin role`() {
        val loginId = "content_admin"
        val password = "Password123!"

        post(
            path = "/api/auth/sign-up",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
                "email" to "content-admin@example.com",
                "nickname" to "관리자",
                "mathStatus" to "UNKNOWN",
            ),
        )
        jdbcTemplate.update("update user_accounts set email_verified = true where login_id = ?", loginId)

        val studentToken = login(loginId, password)
        val rejected = put(
            path = "/api/contents/recovery-mission-templates/admin_test_concept",
            body = recoveryTemplateBody(),
            accessToken = studentToken,
        )
        assertEquals(HttpStatus.UNAUTHORIZED.value(), rejected.statusCode)

        jdbcTemplate.update("update user_accounts set role = 'ADMIN' where login_id = ?", loginId)
        val adminToken = login(loginId, password)
        val accepted = put(
            path = "/api/contents/recovery-mission-templates/admin_test_concept",
            body = recoveryTemplateBody(),
            accessToken = adminToken,
        )

        assertEquals(HttpStatus.OK.value(), accepted.statusCode)
        assertEquals("admin_test_concept", accepted.body.dataMap().string("conceptTag"))
    }

    private fun post(
        path: String,
        body: Any,
        accessToken: String? = null,
    ) = exchange(path, HttpMethod.POST, body, accessToken)

    private fun put(
        path: String,
        body: Any,
        accessToken: String? = null,
    ) = exchange(path, HttpMethod.PUT, body, accessToken)

    private fun login(loginId: String, password: String): String {
        val login = post(
            path = "/api/auth/login",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
            ),
        )
        assertEquals(HttpStatus.OK.value(), login.statusCode)
        return login.body.dataMap().string("accessToken")
    }

    private fun recoveryTemplateBody(): Map<String, Any> {
        return mapOf(
            "title" to "관리자 테스트 미션",
            "prompt" to "관리자 권한으로만 수정돼야 해요.",
            "hints" to listOf("권한을 먼저 확인해요."),
            "estimatedMinutes" to 5,
            "status" to "ACTIVE",
        )
    }

    private fun exchange(
        path: String,
        method: HttpMethod,
        body: Any?,
        accessToken: String? = null,
    ): SmokeResponse {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port$path"))
            .header("Content-Type", "application/json")
        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }
        when (method) {
            HttpMethod.GET -> requestBuilder.GET()
            HttpMethod.POST -> requestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body ?: emptyMap<String, Any>())),
            )
            HttpMethod.PUT -> requestBuilder.PUT(
                HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body ?: emptyMap<String, Any>())),
            )
            else -> error("unsupported method: $method")
        }

        val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        return SmokeResponse(
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

    private fun Map<String, Any?>.list(key: String): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return this[key] as List<Any?>
    }

    private fun Map<String, Any?>.string(key: String): String {
        return this[key] as String
    }

    private fun Map<String, Any?>.number(key: String): Number {
        return this[key] as Number
    }

    @TestConfiguration
    class SmokeTestTokenStoreConfiguration {
        @Bean
        @Primary
        fun smokeTokenStorePort(): TokenStorePort {
            return InMemorySmokeTokenStore()
        }
    }
}

private data class SmokeResponse(
    val statusCode: Int,
    val body: Map<*, *>,
)

private class InMemorySmokeTokenStore : TokenStorePort {
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

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return emailVerificationTokens.remove(tokenId)
    }
}

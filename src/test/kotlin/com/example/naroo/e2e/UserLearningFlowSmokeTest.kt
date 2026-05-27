package com.example.naroo.e2e

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.diagnostic.domain.MathArea
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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "naroo.beta-ops.mode=shadow",
    ],
)
class UserLearningFlowSmokeTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val emailSender: CapturingSmokeEmailSender,
    @LocalServerPort private val port: Int,
) {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
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

        val verification = post(
            path = "/api/auth/email/verify",
            body = mapOf("token" to emailSender.latestTokenFor("smoke@example.com")),
        )
        assertEquals(HttpStatus.OK.value(), verification.statusCode)
        assertEquals(true, verification.body.dataMap()["emailVerified"])

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
        assertEquals("STUDENT", login.body.dataMap().map("user").string("role"))
        val refreshSetCookie = login.refreshSetCookie()
        assertTrue(refreshSetCookie.contains("HttpOnly"))
        assertTrue(refreshSetCookie.contains("Path=/api/auth"))
        assertTrue(refreshSetCookie.contains("SameSite=Strict"))

        val reissue = post(
            path = "/api/auth/reissue",
            body = emptyMap<String, Any>(),
            cookie = login.refreshCookiePair(),
        )
        assertEquals(HttpStatus.OK.value(), reissue.statusCode)
        val reissuedAccessToken = reissue.body.dataMap().string("accessToken")
        assertTrue(reissuedAccessToken.isNotBlank())
        assertTrue(reissue.refreshSetCookie().startsWith("refresh_token="))

        val mathAreas = exchange(
            path = "/api/math-areas",
            method = HttpMethod.GET,
            body = null,
        )
        assertEquals(HttpStatus.OK.value(), mathAreas.statusCode)
        assertEquals(5, mathAreas.body.dataList().size)

        val startingPoint = post(
            path = "/api/diagnostics/starting-point",
            body = mapOf(
                "selectionType" to "WEAK_AREA",
                "mathArea" to "FUNCTION",
                "note" to "함수가 어려워요",
            ),
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), startingPoint.statusCode)

        val diagnosticSession = post(
            path = "/api/diagnostics",
            body = emptyMap<String, Any>(),
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), diagnosticSession.statusCode)
        val diagnosticSessionId = diagnosticSession.body.dataMap().string("id")

        val questions = exchange(
            path = "/api/diagnostics/$diagnosticSessionId/questions",
            method = HttpMethod.GET,
            body = null,
            accessToken = reissuedAccessToken,
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
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.OK.value(), submittedAnswers.statusCode)
        assertEquals("linear_function_slope", submittedAnswers.body.dataMap().string("primaryRecoveryConcept"))

        val result = exchange(
            path = "/api/diagnostics/$diagnosticSessionId/result",
            method = HttpMethod.GET,
            body = null,
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.OK.value(), result.statusCode)
        assertNotNull(result.body.dataMap()["nextMissionPreview"])

        val mission = post(
            path = "/api/recovery-missions",
            body = mapOf("diagnosticSessionId" to diagnosticSessionId),
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), mission.statusCode)
        val missionId = mission.body.dataMap().string("id")

        val submission = post(
            path = "/api/recovery-missions/$missionId/submissions",
            body = mapOf("answerText" to "x 앞에 붙은 숫자를 보고 기울기가 -3이라는 것을 확인했습니다."),
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.CREATED.value(), submission.statusCode)
        assertEquals("COMPLETED", submission.body.dataMap().map("mission").string("status"))

        val home = exchange(
            path = "/api/me/learning-home",
            method = HttpMethod.GET,
            body = null,
            accessToken = reissuedAccessToken,
        )
        assertEquals(HttpStatus.OK.value(), home.statusCode)
        assertEquals("RECOVERY_SERIES_COMPLETED", home.body.dataMap().string("nextAction"))
        assertEquals("COMPLETED", home.body.dataMap().map("latestMission").string("status"))
        assertEquals(1, home.body.dataMap().map("progress").number("completedMissionCount").toInt())
        assertEquals(6, jdbcTemplate.queryForObject("select count(*) from beta_events", Int::class.java))
    }

    @Test
    fun `diagnostic telemetry endpoint records per question beta events`() {
        val accessToken = createVerifiedStudentAndLogin(
            loginId = "telemetry_user",
            password = "Password123!",
            email = "telemetry@example.com",
            nickname = "텔레메트리",
        )

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
        val firstQuestionId = questions.body.dataMap().mapList("questions").first().string("id")

        val questionShown = post(
            path = "/api/diagnostics/$diagnosticSessionId/telemetry",
            body = mapOf(
                "eventType" to "QUESTION_SHOWN",
                "questionId" to firstQuestionId,
                "idempotencyKey" to "question-shown:$diagnosticSessionId:1",
                "occurredAt" to "2026-05-27T06:00:00Z",
                "flowVariant" to "beta-v1",
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.ACCEPTED.value(), questionShown.statusCode)

        val answerSelected = post(
            path = "/api/diagnostics/$diagnosticSessionId/telemetry",
            body = mapOf(
                "eventType" to "ANSWER_SELECTED",
                "questionId" to firstQuestionId,
                "selectedChoiceId" to "b",
                "idempotencyKey" to "answer-selected:$diagnosticSessionId:1",
                "occurredAt" to "2026-05-27T06:00:05Z",
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.ACCEPTED.value(), answerSelected.statusCode)

        val sessionAbandoned = post(
            path = "/api/diagnostics/$diagnosticSessionId/telemetry",
            body = mapOf(
                "eventType" to "SESSION_ABANDONED",
                "questionId" to firstQuestionId,
                "idempotencyKey" to "session-abandoned:$diagnosticSessionId:1",
                "occurredAt" to "2026-05-27T06:00:10Z",
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.ACCEPTED.value(), sessionAbandoned.statusCode)

        assertEquals(6, jdbcTemplate.queryForObject("select count(*) from beta_events", Int::class.java))

        val telemetryEvents = jdbcTemplate.queryForList(
            "select event_type, question_id, flow_variant from beta_events where diagnostic_session_id = ? order by occurred_at asc",
            diagnosticSessionId,
        )
        assertEquals(4, telemetryEvents.size)
        assertTrue(telemetryEvents.any { row -> row["event_type"] == "DIAGNOSTIC_SESSION_CREATED" })
        assertTrue(telemetryEvents.any { row -> row["event_type"] == "DIAGNOSTIC_QUESTION_SHOWN" && row["flow_variant"] == "beta-v1" })
        assertTrue(telemetryEvents.any { row -> row["event_type"] == "DIAGNOSTIC_ANSWER_SELECTED" && row["question_id"] == firstQuestionId })
        assertTrue(telemetryEvents.any { row -> row["event_type"] == "DIAGNOSTIC_SESSION_ABANDONED" && row["question_id"] == firstQuestionId })
    }

    @Test
    fun `diagnostic result trust feedback endpoint records beta event`() {
        val accessToken = createVerifiedStudentAndLogin(
            loginId = "trust_feedback_user",
            password = "Password123!",
            email = "trust-feedback@example.com",
            nickname = "신뢰도",
        )

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

        val trustFeedback = post(
            path = "/api/diagnostics/$diagnosticSessionId/trust-feedback",
            body = mapOf(
                "feedbackChoice" to "FEELS_RIGHT",
                "idempotencyKey" to "trust-feedback:$diagnosticSessionId:1",
                "occurredAt" to "2026-05-27T06:10:00Z",
                "flowVariant" to "beta-v1",
                "resultCopyVersion" to "result-copy-v1",
            ),
            accessToken = accessToken,
        )
        assertEquals(HttpStatus.ACCEPTED.value(), trustFeedback.statusCode)
        assertEquals("APPENDED", trustFeedback.body.dataMap().string("outcome"))

        val trustEvent = jdbcTemplate.queryForMap(
            "select event_type, flow_variant, result_copy_version, payload_json from beta_events where diagnostic_session_id = ? and event_type = ?",
            diagnosticSessionId,
            "DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED",
        )
        assertEquals("beta-v1", trustEvent["flow_variant"])
        assertEquals("result-copy-v1", trustEvent["result_copy_version"])
        assertTrue((trustEvent["payload_json"] as String).contains("\"feedbackChoice\":\"FEELS_RIGHT\""))
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

    @Test
    fun `every default math area can create its first recovery mission`() {
        val accessToken = createVerifiedStudentAndLogin(
            loginId = "content_matrix",
            password = "Password123!",
            email = "content-matrix@example.com",
            nickname = "매트릭스",
        )

        val scenarios = listOf(
            MathArea.EQUATION to "방정식이 어려워요",
            MathArea.FUNCTION to "함수가 어려워요",
            MathArea.GEOMETRY to "도형이 어려워요",
            MathArea.PROBABILITY_AND_STATISTICS to "확률과 통계가 어려워요",
            MathArea.SEQUENCE to "수열이 어려워요",
        )

        scenarios.forEach { (mathArea, note) ->
            val startingPoint = post(
                path = "/api/diagnostics/starting-point",
                body = mapOf(
                    "selectionType" to "WEAK_AREA",
                    "mathArea" to mathArea.name,
                    "note" to note,
                ),
                accessToken = accessToken,
            )
            assertEquals(
                HttpStatus.CREATED.value(),
                startingPoint.statusCode,
                "starting point should be created for ${mathArea.name}",
            )

            val diagnosticSession = post(
                path = "/api/diagnostics",
                body = emptyMap<String, Any>(),
                accessToken = accessToken,
            )
            assertEquals(
                HttpStatus.CREATED.value(),
                diagnosticSession.statusCode,
                "diagnostic session should be created for ${mathArea.name}",
            )
            val diagnosticSessionId = diagnosticSession.body.dataMap().string("id")

            val questions = exchange(
                path = "/api/diagnostics/$diagnosticSessionId/questions",
                method = HttpMethod.GET,
                body = null,
                accessToken = accessToken,
            )
            assertEquals(
                HttpStatus.OK.value(),
                questions.statusCode,
                "questions should load for ${mathArea.name}",
            )
            val questionPayloads = questions.body.dataMap().mapList("questions")
            assertEquals(2, questionPayloads.size, "expected 2 questions for ${mathArea.name}")

            val submittedAnswers = post(
                path = "/api/diagnostics/$diagnosticSessionId/answers",
                body = mapOf(
                    "answers" to questionPayloads.map { questionPayload ->
                        mapOf(
                            "questionId" to questionPayload.string("id"),
                            "selectedChoiceId" to "unknown",
                        )
                    },
                ),
                accessToken = accessToken,
            )
            assertEquals(
                HttpStatus.OK.value(),
                submittedAnswers.statusCode,
                "answers should submit for ${mathArea.name}",
            )

            val mission = post(
                path = "/api/recovery-missions",
                body = mapOf("diagnosticSessionId" to diagnosticSessionId),
                accessToken = accessToken,
            )
            assertEquals(
                HttpStatus.CREATED.value(),
                mission.statusCode,
                "recovery mission should be created for ${mathArea.name}",
            )
            assertEquals(
                diagnosticSessionId,
                mission.body.dataMap().string("diagnosticSessionId"),
                "recovery mission should stay attached to the same diagnostic session for ${mathArea.name}",
            )
            assertTrue(
                mission.body.dataMap().string("conceptTag").isNotBlank(),
                "recovery mission concept should not be blank for ${mathArea.name}",
            )
            assertTrue(
                mission.body.dataMap().string("title").isNotBlank(),
                "recovery mission title should not be blank for ${mathArea.name}",
            )
        }
    }

    private fun post(
        path: String,
        body: Any,
        accessToken: String? = null,
        cookie: String? = null,
    ) = exchange(path, HttpMethod.POST, body, accessToken, cookie)

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

    private fun createVerifiedStudentAndLogin(
        loginId: String,
        password: String,
        email: String,
        nickname: String,
    ): String {
        val signUp = post(
            path = "/api/auth/sign-up",
            body = mapOf(
                "loginId" to loginId,
                "password" to password,
                "email" to email,
                "nickname" to nickname,
                "mathStatus" to "UNKNOWN",
            ),
        )
        assertEquals(HttpStatus.CREATED.value(), signUp.statusCode)

        val verification = post(
            path = "/api/auth/email/verify",
            body = mapOf("token" to emailSender.latestTokenFor(email)),
        )
        assertEquals(HttpStatus.OK.value(), verification.statusCode)

        return login(loginId, password)
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
        cookie: String? = null,
    ): SmokeResponse {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port$path"))
            .header("Content-Type", "application/json")
        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }
        if (!cookie.isNullOrBlank()) {
            requestBuilder.header("Cookie", cookie)
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
            setCookies = response.headers().allValues("Set-Cookie"),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<*, *>.dataMap(): Map<String, Any?> {
        return this["data"] as Map<String, Any?>
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<*, *>.dataList(): List<Any?> {
        return this["data"] as List<Any?>
    }

    private fun Map<String, Any?>.map(key: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return this[key] as Map<String, Any?>
    }

    private fun Map<String, Any?>.list(key: String): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return this[key] as List<Any?>
    }

    private fun Map<String, Any?>.mapList(key: String): List<Map<String, Any?>> {
        return list(key).map { value ->
            @Suppress("UNCHECKED_CAST")
            value as Map<String, Any?>
        }
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

        @Bean
        @Primary
        fun smokeEmailSenderPort(): CapturingSmokeEmailSender {
            return CapturingSmokeEmailSender()
        }
    }
}

private data class SmokeResponse(
    val statusCode: Int,
    val body: Map<*, *>,
    val setCookies: List<String>,
) {
    fun refreshSetCookie(): String {
        return setCookies.first { it.startsWith("refresh_token=") }
    }

    fun refreshCookiePair(): String {
        return refreshSetCookie().substringBefore(";")
    }
}

class CapturingSmokeEmailSender : EmailSenderPort {
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

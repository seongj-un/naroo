package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JpaDiagnosticSessionPersistenceAdapterTest(
    @Autowired private val adapter: JpaDiagnosticSessionPersistenceAdapter,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
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
        jdbcTemplate.update(
            """
                insert into user_accounts (
                    id,
                    login_id,
                    email,
                    email_verified,
                    password_hash,
                    nickname,
                    math_status,
                    created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            "user-1",
            "student01",
            "student01@example.com",
            true,
            "hashed-password",
            "나루",
            "UNKNOWN",
            Instant.parse("2026-04-29T00:00:00Z"),
        )
        jdbcTemplate.update(
            """
                insert into diagnostic_starting_points (
                    id,
                    user_id,
                    selection_type,
                    math_area,
                    note,
                    created_at,
                    updated_at
                ) values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            "starting-point-1",
            "user-1",
            "WEAK_AREA",
            "FUNCTION",
            "함수가 어려워요",
            Instant.parse("2026-04-29T00:00:00Z"),
            Instant.parse("2026-04-29T00:00:00Z"),
        )
    }

    @Test
    fun `saves and finds diagnostic session`() {
        val session = diagnosticSession()

        val saved = adapter.save(session)

        assertEquals(session, saved)
        assertEquals(session, adapter.findById(session.id))
    }

    @Test
    fun `returns null when diagnostic session does not exist`() {
        assertNull(adapter.findById(DiagnosticSessionId("missing-session")))
    }

    private fun diagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            status = DiagnosticSessionStatus.READY,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
            updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }
}

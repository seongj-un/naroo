package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointNote
import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
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
class JpaStartingPointSelectionPersistenceAdapterTest(
    @Autowired private val adapter: JpaStartingPointSelectionPersistenceAdapter,
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
    }

    @Test
    fun `saves and finds starting point by user id`() {
        val selection = startingPointSelection()

        val saved = adapter.save(selection)

        assertEquals(selection, saved)
        assertEquals(selection, adapter.findByUserId(UserId("user-1")))
    }

    @Test
    fun `updates existing starting point`() {
        adapter.save(startingPointSelection())

        val updated = adapter.save(
            startingPointSelection().copy(
                selectionType = StartingPointSelectionType.STUDY_INTEREST,
                mathArea = MathArea.SEQUENCE,
                note = null,
                updatedAt = Instant.parse("2026-04-29T01:00:00Z"),
            ),
        )

        assertEquals(StartingPointSelectionType.STUDY_INTEREST, updated.selectionType)
        assertEquals(MathArea.SEQUENCE, updated.mathArea)
        assertNull(updated.note)
        assertEquals(updated, adapter.findByUserId(UserId("user-1")))
    }

    private fun startingPointSelection(): StartingPointSelection {
        return StartingPointSelection(
            id = StartingPointSelectionId("starting-point-1"),
            userId = UserId("user-1"),
            selectionType = StartingPointSelectionType.WEAK_AREA,
            mathArea = MathArea.FUNCTION,
            note = StartingPointNote.fromNullable("함수가 어려워요"),
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
            updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }
}

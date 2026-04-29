package com.example.naroo.user.adapter.`out`.persistence

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.DuplicateUserAccountException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JpaUserAccountPersistenceAdapterTest(
    @Autowired private val adapter: JpaUserAccountPersistenceAdapter,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute(
            """
                create table if not exists user_accounts (
                    id varchar(36) primary key,
                    login_id varchar(30) not null unique,
                    password_hash varchar(255) not null,
                    nickname varchar(20) not null,
                    math_status varchar(32) not null,
                    created_at datetime(6) not null
                )
            """.trimIndent(),
        )
        jdbcTemplate.update("delete from user_accounts")
    }

    @Test
    fun `saves and finds user account by login id and user id`() {
        val userAccount = userAccount()

        val saved = adapter.save(userAccount)

        assertEquals(userAccount, saved)
        assertTrue(adapter.existsByLoginId(userAccount.loginId))
        assertEquals(userAccount, adapter.findByLoginId(userAccount.loginId))
        assertEquals(userAccount, adapter.findById(userAccount.id))
    }

    @Test
    fun `returns null and false when user account does not exist`() {
        assertFalse(adapter.existsByLoginId(LoginId.from("missing-user")))
        assertNull(adapter.findByLoginId(LoginId.from("missing-user")))
        assertNull(adapter.findById(UserId("missing-user-id")))
    }

    @Test
    fun `throws duplicate user account exception when login id already exists`() {
        val userAccount = userAccount()
        adapter.save(userAccount)

        assertThrows(DuplicateUserAccountException::class.java) {
            adapter.save(userAccount.copy(id = UserId("user-2")))
        }
    }

    private fun userAccount(): UserAccount {
        return UserAccount(
            id = UserId("user-1"),
            loginId = LoginId.from("student01"),
            passwordHash = PasswordHash("hashed-password"),
            nickname = Nickname.from("나루"),
            mathStatus = MathStatus.MOSTLY_GAVE_UP,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }
}

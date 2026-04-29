package com.example.naroo.auth.application.service

import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SignUpUserServiceTest {
    private val repository = FakeUserAccountRepository()
    private val service = SignUpUserService(
        userAccountRepositoryPort = repository,
        passwordHasherPort = PasswordHasherPort { PasswordHash("hashed:${it.length}") },
        userIdGeneratorPort = UserIdGeneratorPort { UserId("user-1") },
        clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
    )

    @Test
    fun `signs up a user account`() {
        val result = service.signUp(
            SignUpUserCommand(
                loginId = "student01",
                password = "password123",
                nickname = "나루",
                mathStatus = MathStatus.MOSTLY_GAVE_UP,
            ),
        )

        assertEquals("user-1", result.id.value)
        assertEquals("student01", result.loginId)
        assertEquals("나루", result.nickname)
        assertEquals(MathStatus.MOSTLY_GAVE_UP, result.mathStatus)
        assertEquals(Instant.parse("2026-04-29T00:00:00Z"), result.createdAt)
        assertEquals("hashed:11", repository.saved.single().passwordHash.value)
    }

    @Test
    fun `rejects duplicate login id`() {
        val command = SignUpUserCommand(
            loginId = "student01",
            password = "password123",
            nickname = "나루",
            mathStatus = MathStatus.UNKNOWN,
        )

        service.signUp(command)

        assertThrows(DuplicateLoginIdException::class.java) {
            service.signUp(command.copy(nickname = "다른나루"))
        }
    }

    @Test
    fun `rejects short password`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.signUp(
                SignUpUserCommand(
                    loginId = "student02",
                    password = "short",
                    nickname = "나루",
                    mathStatus = MathStatus.UNKNOWN,
                ),
            )
        }
    }
}

private class FakeUserAccountRepository : UserAccountRepositoryPort {
    val saved = mutableListOf<UserAccount>()

    override fun existsByLoginId(loginId: LoginId): Boolean {
        return saved.any { it.loginId == loginId }
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return saved.firstOrNull { it.loginId == loginId }
    }

    override fun save(userAccount: UserAccount): UserAccount {
        saved += userAccount
        return userAccount
    }
}

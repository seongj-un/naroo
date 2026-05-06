package com.example.naroo.user.application

import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.DuplicateUserAccountException
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.DefaultApplicationArguments
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BetaStudentSeedRunnerTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-06T00:00:00Z"), ZoneOffset.UTC)
    private val args = DefaultApplicationArguments(*emptyArray<String>())

    @Test
    fun `does nothing when seed is disabled`() {
        val repository = FakeUserAccountRepository()

        runner(repository, enabled = false).run(args)

        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `creates verified beta student when seed is enabled`() {
        val repository = FakeUserAccountRepository()

        runner(repository, enabled = true).run(args)

        val saved = repository.saved.single()
        assertEquals("seed-user-1", saved.id.value)
        assertEquals("student01", saved.loginId.value)
        assertEquals("student01@example.com", saved.email.value)
        assertEquals(true, saved.emailVerified)
        assertEquals("hashed:password123", saved.passwordHash.value)
        assertEquals("나루", saved.nickname.value)
        assertEquals(MathStatus.UNKNOWN, saved.mathStatus)
        assertEquals(Instant.parse("2026-05-06T00:00:00Z"), saved.createdAt)
    }

    @Test
    fun `skips seed when login id already exists`() {
        val repository = FakeUserAccountRepository(
            existingLoginIds = mutableSetOf("student01"),
        )

        runner(repository, enabled = true).run(args)

        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `skips seed when email already exists`() {
        val repository = FakeUserAccountRepository(
            existingEmails = mutableSetOf("student01@example.com"),
        )

        runner(repository, enabled = true).run(args)

        assertTrue(repository.saved.isEmpty())
    }

    private fun runner(
        repository: FakeUserAccountRepository,
        enabled: Boolean,
    ): BetaStudentSeedRunner {
        return BetaStudentSeedRunner(
            userAccountRepositoryPort = repository,
            passwordHasherPort = PasswordHasherPort { PasswordHash("hashed:$it") },
            userIdGeneratorPort = UserIdGeneratorPort { UserId("seed-user-1") },
            clock = clock,
            enabled = enabled,
            seedLoginId = "student01",
            seedEmail = "student01@example.com",
            seedPassword = "password123",
            seedNickname = "나루",
            seedMathStatus = MathStatus.UNKNOWN,
        )
    }
}

private class FakeUserAccountRepository(
    private val existingLoginIds: MutableSet<String> = mutableSetOf(),
    private val existingEmails: MutableSet<String> = mutableSetOf(),
) : UserAccountRepositoryPort {
    val saved = mutableListOf<UserAccount>()

    override fun existsByLoginId(loginId: LoginId): Boolean {
        return loginId.value in existingLoginIds
    }

    override fun existsByEmail(email: EmailAddress): Boolean {
        return email.value in existingEmails
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return null
    }

    override fun findById(userId: UserId): UserAccount? {
        return null
    }

    override fun save(userAccount: UserAccount): UserAccount {
        if (!existingLoginIds.add(userAccount.loginId.value) || !existingEmails.add(userAccount.email.value)) {
            throw DuplicateUserAccountException("loginId or email already exists")
        }
        saved += userAccount
        return userAccount
    }
}

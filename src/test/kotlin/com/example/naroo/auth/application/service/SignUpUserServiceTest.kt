package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.IssuedEmailVerificationToken
import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SignUpUserServiceTest {
    private val repository = FakeUserAccountRepository()
    private val tokenStore = CapturingSignUpTokenStore()
    private val emailSender = CapturingEmailSender()
    private val service = SignUpUserService(
        userAccountRepositoryPort = repository,
        passwordHasherPort = PasswordHasherPort { PasswordHash("hashed:${it.length}") },
        userIdGeneratorPort = UserIdGeneratorPort { UserId("user-1") },
        emailVerificationTokenPort = FakeEmailVerificationTokenPort(),
        tokenStorePort = tokenStore,
        emailSenderPort = emailSender,
        clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
    )

    @Test
    fun `signs up a user account`() {
        val result = service.signUp(
            SignUpUserCommand(
                loginId = "student01",
                email = "student01@example.com",
                password = "password123",
                nickname = "나루",
                mathStatus = MathStatus.MOSTLY_GAVE_UP,
            ),
        )

        assertEquals("user-1", result.id.value)
        assertEquals("student01", result.loginId)
        assertEquals("student01@example.com", result.email)
        assertEquals(false, result.emailVerified)
        assertEquals("나루", result.nickname)
        assertEquals(MathStatus.MOSTLY_GAVE_UP, result.mathStatus)
        assertEquals(Instant.parse("2026-04-29T00:00:00Z"), result.createdAt)
        assertEquals("hashed:11", repository.saved.single().passwordHash.value)
        assertEquals("email-token-1", tokenStore.emailVerificationTokens.single().tokenId)
        assertEquals("user-1", tokenStore.emailVerificationTokens.single().userId)
        assertEquals("student01@example.com", emailSender.messages.single().email)
        assertEquals("email-token-1.secret", emailSender.messages.single().token)
    }

    @Test
    fun `rejects duplicate login id`() {
        val command = SignUpUserCommand(
            loginId = "student01",
            email = "student01@example.com",
            password = "password123",
            nickname = "나루",
            mathStatus = MathStatus.UNKNOWN,
        )

        service.signUp(command)

        assertThrows(AuthException.LoginIdAlreadyExists::class.java) {
            service.signUp(command.copy(nickname = "다른나루"))
        }
    }

    @Test
    fun `rejects short password`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.signUp(
                SignUpUserCommand(
                    loginId = "student02",
                    email = "student02@example.com",
                    password = "short",
                    nickname = "나루",
                    mathStatus = MathStatus.UNKNOWN,
                ),
            )
        }
    }

    @Test
    fun `cleans up saved user and token when email delivery fails`() {
        val cleanupRepository = FakeUserAccountRepository()
        val cleanupTokenStore = CapturingSignUpTokenStore()
        val failingService = SignUpUserService(
            userAccountRepositoryPort = cleanupRepository,
            passwordHasherPort = PasswordHasherPort { PasswordHash("hashed:${it.length}") },
            userIdGeneratorPort = UserIdGeneratorPort { UserId("user-cleanup") },
            emailVerificationTokenPort = FakeEmailVerificationTokenPort(),
            tokenStorePort = cleanupTokenStore,
            emailSenderPort = EmailSenderPort { throw IllegalStateException("email send failed") },
            clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(IllegalStateException::class.java) {
            failingService.signUp(
                SignUpUserCommand(
                    loginId = "student03",
                    email = "student03@example.com",
                    password = "password123",
                    nickname = "나루",
                    mathStatus = MathStatus.UNKNOWN,
                ),
            )
        }

        assertTrue(cleanupRepository.saved.isEmpty())
        assertEquals(listOf("email-token-1"), cleanupTokenStore.deletedEmailVerificationTokenIds)
    }
}

private class FakeEmailVerificationTokenPort : EmailVerificationTokenPort {
    override fun issue(userId: String, email: String): IssuedEmailVerificationToken {
        return IssuedEmailVerificationToken(
            id = "email-token-1",
            value = "email-token-1.secret",
            tokenHash = "email-token-hash",
            expiresAt = Instant.parse("2026-04-29T00:30:00Z"),
        )
    }

    override fun hash(rawToken: String): String {
        return "email-token-hash"
    }
}

private class CapturingSignUpTokenStore : TokenStorePort {
    val emailVerificationTokens = mutableListOf<StoredEmailVerificationToken>()
    val deletedEmailVerificationTokenIds = mutableListOf<String>()

    override fun saveAccessToken(token: StoredAccessToken) {
        error("access token should not be stored")
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        error("access token should not be read")
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        error("refresh token should not be stored")
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        error("refresh token should not be consumed")
    }

    override fun saveEmailVerificationToken(token: StoredEmailVerificationToken) {
        emailVerificationTokens += token
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        error("email verification token should not be consumed")
    }

    override fun deleteEmailVerificationToken(tokenId: String) {
        deletedEmailVerificationTokenIds += tokenId
        emailVerificationTokens.removeIf { it.tokenId == tokenId }
    }
}

private class CapturingEmailSender : EmailSenderPort {
    val messages = mutableListOf<EmailVerificationMessage>()

    override fun sendEmailVerification(message: EmailVerificationMessage) {
        messages += message
    }
}

private class FakeUserAccountRepository : UserAccountRepositoryPort {
    val saved = mutableListOf<UserAccount>()

    override fun existsByLoginId(loginId: LoginId): Boolean {
        return saved.any { it.loginId == loginId }
    }

    override fun existsByEmail(email: EmailAddress): Boolean {
        return saved.any { it.email == email }
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return saved.firstOrNull { it.loginId == loginId }
    }

    override fun findById(userId: UserId): UserAccount? {
        return saved.firstOrNull { it.id == userId }
    }

    override fun save(userAccount: UserAccount): UserAccount {
        saved += userAccount
        return userAccount
    }

    override fun deleteById(userId: UserId) {
        saved.removeIf { it.id == userId }
    }
}

package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.IssuedEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.auth.port.`in`.ResendEmailVerificationCommand
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ResendEmailVerificationServiceTest {
    private val userAccount = UserAccount(
        id = UserId("user-1"),
        loginId = LoginId.from("student01"),
        email = EmailAddress.from("student01@example.com"),
        emailVerified = false,
        passwordHash = PasswordHash("hashed-password"),
        nickname = Nickname.from("나루"),
        mathStatus = MathStatus.UNKNOWN,
        createdAt = Instant.parse("2026-04-29T00:00:00Z"),
    )

    @Test
    fun `resends verification email for authenticated unverified user`() {
        val tokenStore = CapturingResendTokenStore()
        val emailSender = CapturingResendEmailSender()
        val service = ResendEmailVerificationService(
            userAccountRepositoryPort = CapturingResendRepository(userAccount),
            emailVerificationTokenPort = FakeResendEmailVerificationTokenPort(),
            tokenStorePort = tokenStore,
            emailSenderPort = emailSender,
            resendCooldownSeconds = 90,
            clock = Clock.fixed(Instant.parse("2026-05-21T06:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.resend(ResendEmailVerificationCommand(userId = "user-1"))

        assertEquals("user-1", result.userId)
        assertEquals("student01@example.com", result.email)
        assertEquals(false, result.emailVerified)
        assertEquals(Instant.parse("2026-05-21T06:01:30Z"), result.nextRetryAt)
        assertEquals("email-token-2", tokenStore.emailVerificationTokens.single().tokenId)
        assertEquals("student01@example.com", emailSender.messages.single().email)
        assertEquals(Instant.parse("2026-05-21T06:01:30Z"), tokenStore.cooldownsByUserId["user-1"])
    }

    @Test
    fun `rejects resend when user is already verified`() {
        val service = ResendEmailVerificationService(
            userAccountRepositoryPort = CapturingResendRepository(userAccount.copy(emailVerified = true)),
            emailVerificationTokenPort = FakeResendEmailVerificationTokenPort(),
            tokenStorePort = CapturingResendTokenStore(),
            emailSenderPort = CapturingResendEmailSender(),
            clock = Clock.fixed(Instant.parse("2026-05-21T06:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(AuthException.EmailAlreadyVerified::class.java) {
            service.resend(ResendEmailVerificationCommand(userId = "user-1"))
        }
    }

    @Test
    fun `rejects resend during cooldown window`() {
        val tokenStore = CapturingResendTokenStore()
        tokenStore.cooldownsByUserId["user-1"] = Instant.parse("2026-05-21T06:01:00Z")
        val service = ResendEmailVerificationService(
            userAccountRepositoryPort = CapturingResendRepository(userAccount),
            emailVerificationTokenPort = FakeResendEmailVerificationTokenPort(),
            tokenStorePort = tokenStore,
            emailSenderPort = CapturingResendEmailSender(),
            clock = Clock.fixed(Instant.parse("2026-05-21T06:00:00Z"), ZoneOffset.UTC),
        )

        val exception = assertThrows(AuthException.EmailVerificationResendTooSoon::class.java) {
            service.resend(ResendEmailVerificationCommand(userId = "user-1"))
        }

        assertEquals(60, exception.retryAfterSeconds)
    }
}

private class FakeResendEmailVerificationTokenPort : EmailVerificationTokenPort {
    override fun issue(userId: String, email: String): IssuedEmailVerificationToken {
        return IssuedEmailVerificationToken(
            id = "email-token-2",
            value = "email-token-2.secret",
            tokenHash = "email-token-hash-2",
            expiresAt = Instant.parse("2026-05-21T06:30:00Z"),
        )
    }

    override fun hash(rawToken: String): String {
        return "email-token-hash-2"
    }
}

private class CapturingResendTokenStore : TokenStorePort {
    val emailVerificationTokens = mutableListOf<StoredEmailVerificationToken>()
    val cooldownsByUserId = mutableMapOf<String, Instant>()

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
        emailVerificationTokens.removeIf { it.tokenId == tokenId }
    }

    override fun saveEmailVerificationResendCooldown(userId: String, availableAt: Instant) {
        cooldownsByUserId[userId] = availableAt
    }

    override fun findEmailVerificationResendAvailableAt(userId: String): Instant? {
        return cooldownsByUserId[userId]
    }
}

private class CapturingResendEmailSender : EmailSenderPort {
    val messages = mutableListOf<EmailVerificationMessage>()

    override fun sendEmailVerification(message: EmailVerificationMessage) {
        messages += message
    }
}

private class CapturingResendRepository(
    private val userAccount: UserAccount?,
) : UserAccountRepositoryPort {
    override fun existsByLoginId(loginId: LoginId): Boolean {
        return userAccount?.loginId == loginId
    }

    override fun existsByEmail(email: EmailAddress): Boolean {
        return userAccount?.email == email
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return userAccount?.takeIf { it.loginId == loginId }
    }

    override fun findById(userId: UserId): UserAccount? {
        return userAccount?.takeIf { it.id == userId }
    }

    override fun save(userAccount: UserAccount): UserAccount {
        error("user account should not be saved")
    }
}

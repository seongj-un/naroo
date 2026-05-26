package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.VerifyEmailCommand
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.IssuedEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
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
import java.time.Instant

class VerifyEmailServiceTest {
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
    fun `verifies email with a valid single-use token`() {
        val repository = CapturingVerifyEmailRepository(userAccount)
        val tokenStore = CapturingVerifyEmailTokenStore(
            StoredEmailVerificationToken(
                tokenId = "email-token-1",
                userId = "user-1",
                email = "student01@example.com",
                tokenHash = "email-token-hash",
                expiresAt = Instant.parse("2026-04-29T00:30:00Z"),
            ),
        )
        val service = VerifyEmailService(
            userAccountRepositoryPort = repository,
            emailVerificationTokenPort = FakeVerifyEmailTokenPort(),
            tokenStorePort = tokenStore,
        )

        val result = service.verify(VerifyEmailCommand(token = "email-token-1.secret"))

        assertEquals("user-1", result.userId)
        assertEquals("student01@example.com", result.email)
        assertEquals(true, result.emailVerified)
        assertEquals(true, repository.saved.single().emailVerified)
        assertThrows(AuthException.InvalidEmailVerificationToken::class.java) {
            service.verify(VerifyEmailCommand(token = "email-token-1.secret"))
        }
    }

    @Test
    fun `rejects token with invalid hash without consuming it`() {
        val tokenStore = CapturingVerifyEmailTokenStore(
            StoredEmailVerificationToken(
                tokenId = "email-token-1",
                userId = "user-1",
                email = "student01@example.com",
                tokenHash = "different-hash",
                expiresAt = Instant.parse("2026-04-29T00:30:00Z"),
            ),
        )
        val service = VerifyEmailService(
            userAccountRepositoryPort = CapturingVerifyEmailRepository(userAccount),
            emailVerificationTokenPort = FakeVerifyEmailTokenPort(),
            tokenStorePort = tokenStore,
        )

        assertThrows(AuthException.InvalidEmailVerificationToken::class.java) {
            service.verify(VerifyEmailCommand(token = "email-token-1.secret"))
        }
        assertEquals(
            "different-hash",
            tokenStore.findEmailVerificationToken("email-token-1")?.tokenHash,
        )
    }
}

private class FakeVerifyEmailTokenPort : EmailVerificationTokenPort {
    override fun issue(userId: String, email: String): IssuedEmailVerificationToken {
        error("email verification token should not be issued")
    }

    override fun hash(rawToken: String): String {
        return when (rawToken) {
            "email-token-1.secret" -> "email-token-hash"
            else -> "unknown-hash"
        }
    }
}

private class CapturingVerifyEmailTokenStore(
    token: StoredEmailVerificationToken,
) : TokenStorePort {
    private val tokensById = mutableMapOf(token.tokenId to token)

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
        tokensById[token.tokenId] = token
    }

    override fun findEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return tokensById[tokenId]
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        return tokensById.remove(tokenId)
    }

    override fun deleteEmailVerificationToken(tokenId: String) {
        tokensById.remove(tokenId)
    }
}

private class CapturingVerifyEmailRepository(
    private var userAccount: UserAccount?,
) : UserAccountRepositoryPort {
    val saved = mutableListOf<UserAccount>()

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
        this.userAccount = userAccount
        saved += userAccount
        return userAccount
    }
}

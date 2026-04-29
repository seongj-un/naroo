package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.ReissueTokenCommand
import com.example.naroo.auth.port.`out`.IssuedJwtToken
import com.example.naroo.auth.port.`out`.IssuedRefreshToken
import com.example.naroo.auth.port.`out`.JwtTokenIssuerPort
import com.example.naroo.auth.port.`out`.RefreshTokenPort
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

class ReissueTokenServiceTest {
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
    fun `reissues access token and rotates refresh token once`() {
        val tokenStore = CapturingReissueTokenStore(
            StoredRefreshToken(
                tokenId = "refresh-1",
                userId = "user-1",
                tokenHash = "old-refresh-hash",
                expiresAt = Instant.parse("2026-05-02T00:00:00Z"),
            ),
        )
        val service = ReissueTokenService(
            userAccountRepositoryPort = FakeReissueUserRepository(userAccount),
            jwtTokenIssuerPort = JwtTokenIssuerPort {
                IssuedJwtToken(
                    id = "access-2",
                    value = "new-access-token",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                )
            },
            refreshTokenPort = FakeReissueRefreshTokenPort(),
            tokenStorePort = tokenStore,
        )

        val result = service.reissue(ReissueTokenCommand(refreshToken = "refresh-1.old-secret"))

        assertEquals("new-access-token", result.accessToken)
        assertEquals("Bearer", result.tokenType)
        assertEquals("refresh-2.new-secret", result.refreshToken)
        assertEquals(1, tokenStore.savedAccessTokens.size)
        assertEquals("access-2", tokenStore.savedAccessTokens.single().tokenId)
        assertEquals(1, tokenStore.savedRefreshTokens.size)
        assertEquals("refresh-2", tokenStore.savedRefreshTokens.single().tokenId)

        assertThrows(AuthException.InvalidRefreshToken::class.java) {
            service.reissue(ReissueTokenCommand(refreshToken = "refresh-1.old-secret"))
        }
    }

    @Test
    fun `rejects refresh token with invalid hash and still consumes it`() {
        val tokenStore = CapturingReissueTokenStore(
            StoredRefreshToken(
                tokenId = "refresh-1",
                userId = "user-1",
                tokenHash = "different-hash",
                expiresAt = Instant.parse("2026-05-02T00:00:00Z"),
            ),
        )
        val service = ReissueTokenService(
            userAccountRepositoryPort = FakeReissueUserRepository(userAccount),
            jwtTokenIssuerPort = JwtTokenIssuerPort { error("access token should not be issued") },
            refreshTokenPort = FakeReissueRefreshTokenPort(),
            tokenStorePort = tokenStore,
        )

        assertThrows(AuthException.InvalidRefreshToken::class.java) {
            service.reissue(ReissueTokenCommand(refreshToken = "refresh-1.old-secret"))
        }
        assertThrows(AuthException.InvalidRefreshToken::class.java) {
            service.reissue(ReissueTokenCommand(refreshToken = "refresh-1.old-secret"))
        }
    }
}

private class FakeReissueRefreshTokenPort : RefreshTokenPort {
    override fun issue(userId: String): IssuedRefreshToken {
        return IssuedRefreshToken(
            id = "refresh-2",
            value = "refresh-2.new-secret",
            tokenHash = "new-refresh-hash",
            expiresAt = Instant.parse("2026-05-02T00:00:00Z"),
        )
    }

    override fun hash(rawToken: String): String {
        return when (rawToken) {
            "refresh-1.old-secret" -> "old-refresh-hash"
            "refresh-2.new-secret" -> "new-refresh-hash"
            else -> "unknown-hash"
        }
    }
}

private class CapturingReissueTokenStore(
    refreshToken: StoredRefreshToken,
) : TokenStorePort {
    private val refreshTokensById = mutableMapOf(refreshToken.tokenId to refreshToken)
    val savedAccessTokens = mutableListOf<StoredAccessToken>()
    val savedRefreshTokens = mutableListOf<StoredRefreshToken>()

    override fun saveAccessToken(token: StoredAccessToken) {
        savedAccessTokens += token
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        return savedAccessTokens.firstOrNull { it.tokenId == tokenId }?.userId
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        refreshTokensById[token.tokenId] = token
        savedRefreshTokens += token
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        return refreshTokensById.remove(tokenId)
    }

    override fun saveEmailVerificationToken(token: StoredEmailVerificationToken) {
        error("email verification token should not be stored")
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        error("email verification token should not be consumed")
    }
}

private class FakeReissueUserRepository(
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
        return userAccount
    }
}

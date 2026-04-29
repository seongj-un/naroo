package com.example.naroo.auth.application.service

import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`out`.IssuedJwtToken
import com.example.naroo.auth.port.`out`.JwtTokenIssuerPort
import com.example.naroo.auth.port.`out`.PasswordVerifierPort
import com.example.naroo.auth.port.`out`.IssuedRefreshToken
import com.example.naroo.auth.port.`out`.RefreshTokenPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
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

class LoginUserServiceTest {
    private val userAccount = UserAccount(
        id = UserId("user-1"),
        loginId = LoginId.from("student01"),
        passwordHash = PasswordHash("hashed-password"),
        nickname = Nickname.from("나루"),
        mathStatus = MathStatus.BARELY_FOLLOWS,
        createdAt = Instant.parse("2026-04-29T00:00:00Z"),
    )

    @Test
    fun `logs in with valid credentials and returns jwt token`() {
        val service = LoginUserService(
            userAccountRepositoryPort = FakeLoginUserRepository(userAccount),
            passwordVerifierPort = PasswordVerifierPort { rawPassword, passwordHash ->
                rawPassword == "password123" && passwordHash.value == "hashed-password"
            },
            jwtTokenIssuerPort = JwtTokenIssuerPort {
                IssuedJwtToken(
                    id = "token-1",
                    value = "jwt-token",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                )
            },
            refreshTokenPort = FakeRefreshTokenPort(),
            tokenStorePort = CapturingTokenStore(),
        )

        val result = service.login(
            LoginUserCommand(
                loginId = "student01",
                password = "password123",
            ),
        )

        assertEquals("jwt-token", result.accessToken)
        assertEquals("Bearer", result.tokenType)
        assertEquals(Instant.parse("2026-04-29T01:00:00Z"), result.expiresAt)
        assertEquals("refresh-1.secret", result.refreshToken)
        assertEquals(Instant.parse("2026-05-02T00:00:00Z"), result.refreshTokenExpiresAt)
        assertEquals("user-1", result.user.id)
        assertEquals("student01", result.user.loginId)
        assertEquals("나루", result.user.nickname)
        assertEquals(MathStatus.BARELY_FOLLOWS, result.user.mathStatus)
    }

    @Test
    fun `rejects unknown login id`() {
        val service = LoginUserService(
            userAccountRepositoryPort = FakeLoginUserRepository(null),
            passwordVerifierPort = PasswordVerifierPort { _, _ -> true },
            jwtTokenIssuerPort = JwtTokenIssuerPort { error("token should not be issued") },
            refreshTokenPort = FakeRefreshTokenPort(),
            tokenStorePort = RejectingTokenStore(),
        )

        assertThrows(InvalidLoginCredentialsException::class.java) {
            service.login(LoginUserCommand(loginId = "student01", password = "password123"))
        }
    }

    @Test
    fun `rejects invalid password`() {
        val service = LoginUserService(
            userAccountRepositoryPort = FakeLoginUserRepository(userAccount),
            passwordVerifierPort = PasswordVerifierPort { _, _ -> false },
            jwtTokenIssuerPort = JwtTokenIssuerPort { error("token should not be issued") },
            refreshTokenPort = FakeRefreshTokenPort(),
            tokenStorePort = RejectingTokenStore(),
        )

        assertThrows(InvalidLoginCredentialsException::class.java) {
            service.login(LoginUserCommand(loginId = "student01", password = "wrong-password"))
        }
    }
}

private class CapturingTokenStore : TokenStorePort {
    val accessTokens = mutableListOf<StoredAccessToken>()
    val refreshTokens = mutableListOf<StoredRefreshToken>()

    override fun saveAccessToken(token: StoredAccessToken) {
        accessTokens += token
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        return accessTokens.firstOrNull { it.tokenId == tokenId }?.userId
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        refreshTokens += token
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        return refreshTokens.removeAt(refreshTokens.indexOfFirst { it.tokenId == tokenId })
    }
}

private class RejectingTokenStore : TokenStorePort {
    override fun saveAccessToken(token: StoredAccessToken) {
        error("token should not be stored")
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        error("token should not be read")
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        error("refresh token should not be stored")
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        error("refresh token should not be consumed")
    }
}

private class FakeRefreshTokenPort : RefreshTokenPort {
    override fun issue(userId: String): IssuedRefreshToken {
        return IssuedRefreshToken(
            id = "refresh-1",
            value = "refresh-1.secret",
            tokenHash = "refresh-hash",
            expiresAt = Instant.parse("2026-05-02T00:00:00Z"),
        )
    }

    override fun hash(rawToken: String): String {
        return "refresh-hash"
    }
}

private class FakeLoginUserRepository(
    private val userAccount: UserAccount?,
) : UserAccountRepositoryPort {
    override fun existsByLoginId(loginId: LoginId): Boolean {
        return userAccount?.loginId == loginId
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

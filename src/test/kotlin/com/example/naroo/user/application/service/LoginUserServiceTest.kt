package com.example.naroo.user.application.service

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`in`.LoginUserCommand
import com.example.naroo.user.port.`out`.IssuedJwtToken
import com.example.naroo.user.port.`out`.JwtTokenIssuerPort
import com.example.naroo.user.port.`out`.PasswordVerifierPort
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
                    value = "jwt-token",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                )
            },
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
        )

        assertThrows(InvalidLoginCredentialsException::class.java) {
            service.login(LoginUserCommand(loginId = "student01", password = "wrong-password"))
        }
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

    override fun save(userAccount: UserAccount): UserAccount {
        return userAccount
    }
}

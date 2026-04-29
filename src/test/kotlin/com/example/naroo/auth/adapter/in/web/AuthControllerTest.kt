package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`in`.LoggedInUser
import com.example.naroo.auth.port.`in`.LoggedInUserResult
import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.auth.port.`in`.ReissueTokenCommand
import com.example.naroo.auth.port.`in`.ReissueTokenUseCase
import com.example.naroo.auth.port.`in`.ReissuedTokenResult
import com.example.naroo.auth.port.`in`.SignedUpUserResult
import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`in`.SignUpUserUseCase
import com.example.naroo.auth.port.`in`.VerifiedEmailResult
import com.example.naroo.auth.port.`in`.VerifyEmailCommand
import com.example.naroo.auth.port.`in`.VerifyEmailUseCase
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

class AuthControllerTest {
    @Test
    fun `sign-up returns created user response`() {
        var capturedCommand: SignUpUserCommand? = null
        val controller = AuthController(
            SignUpUserUseCase { command ->
                capturedCommand = command
                SignedUpUserResult(
                    id = UserId("user-1"),
                    loginId = command.loginId,
                    email = command.email,
                    emailVerified = false,
                    nickname = command.nickname,
                    mathStatus = command.mathStatus,
                    createdAt = Instant.parse("2026-04-29T00:00:00Z"),
                )
            },
            LoginUserUseCase { error("login should not be called") },
            ReissueTokenUseCase { error("reissue should not be called") },
            VerifyEmailUseCase { error("verify email should not be called") },
        )

        val response = controller.signUp(
            SignUpUserRequest(
                loginId = "student01",
                email = "student01@example.com",
                password = "password123",
                nickname = "나루",
                mathStatus = MathStatus.MOSTLY_GAVE_UP,
            ),
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("student01", capturedCommand?.loginId)
        assertEquals("student01@example.com", capturedCommand?.email)
        assertEquals("password123", capturedCommand?.password)
        assertEquals("student01@example.com", response.body?.data?.email)
        assertEquals(false, response.body?.data?.emailVerified)
        assertEquals("나루", response.body?.data?.nickname)
        assertEquals(MathStatus.MOSTLY_GAVE_UP, response.body?.data?.mathStatus)
    }

    @Test
    fun `login returns bearer access token response`() {
        var capturedCommand: LoginUserCommand? = null
        val controller = AuthController(
            SignUpUserUseCase { error("sign-up should not be called") },
            LoginUserUseCase { command ->
                capturedCommand = command
                LoggedInUserResult(
                    accessToken = "jwt-token",
                    tokenType = "Bearer",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                    refreshToken = "refresh-token",
                    refreshTokenExpiresAt = Instant.parse("2026-05-02T00:00:00Z"),
                    user = LoggedInUser(
                        id = "user-1",
                        loginId = command.loginId,
                        email = "student01@example.com",
                        emailVerified = false,
                        nickname = "나루",
                        mathStatus = MathStatus.UNKNOWN,
                    ),
                )
            },
            ReissueTokenUseCase { error("reissue should not be called") },
            VerifyEmailUseCase { error("verify email should not be called") },
        )

        val response = controller.login(
            LoginUserRequest(
                loginId = "student01",
                password = "password123",
            ),
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("student01", capturedCommand?.loginId)
        assertEquals("password123", capturedCommand?.password)
        assertEquals("jwt-token", response.body?.data?.accessToken)
        assertEquals("Bearer", response.body?.data?.tokenType)
        assertEquals("user-1", response.body?.data?.user?.id)
        assertEquals("student01@example.com", response.body?.data?.user?.email)
        assertEquals(false, response.body?.data?.user?.emailVerified)
        assertEquals(true, response.headers["Set-Cookie"]?.single()?.contains("refresh_token=refresh-token"))
    }

    @Test
    fun `reissue consumes refresh token and returns new access token response`() {
        var capturedCommand: ReissueTokenCommand? = null
        val controller = AuthController(
            SignUpUserUseCase { error("sign-up should not be called") },
            LoginUserUseCase { error("login should not be called") },
            ReissueTokenUseCase { command ->
                capturedCommand = command
                ReissuedTokenResult(
                    accessToken = "new-jwt-token",
                    tokenType = "Bearer",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                    refreshToken = "new-refresh-token",
                    refreshTokenExpiresAt = Instant.parse("2026-05-02T00:00:00Z"),
                )
            },
            VerifyEmailUseCase { error("verify email should not be called") },
        )

        val response = controller.reissue("refresh-token")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("refresh-token", capturedCommand?.refreshToken)
        assertEquals("new-jwt-token", response.body?.data?.accessToken)
        assertEquals("Bearer", response.body?.data?.tokenType)
        assertEquals(true, response.headers["Set-Cookie"]?.single()?.contains("refresh_token=new-refresh-token"))
    }

    @Test
    fun `verify email returns verified email response`() {
        var capturedCommand: VerifyEmailCommand? = null
        val controller = AuthController(
            SignUpUserUseCase { error("sign-up should not be called") },
            LoginUserUseCase { error("login should not be called") },
            ReissueTokenUseCase { error("reissue should not be called") },
            VerifyEmailUseCase { command ->
                capturedCommand = command
                VerifiedEmailResult(
                    userId = "user-1",
                    email = "student01@example.com",
                    emailVerified = true,
                )
            },
        )

        val response = controller.verifyEmail(VerifyEmailRequest(token = "email-token"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("email-token", capturedCommand?.token)
        assertEquals("user-1", response.body?.data?.userId)
        assertEquals("student01@example.com", response.body?.data?.email)
        assertEquals(true, response.body?.data?.emailVerified)
    }
}

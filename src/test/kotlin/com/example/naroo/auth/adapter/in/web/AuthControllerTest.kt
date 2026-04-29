package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`in`.LoggedInUser
import com.example.naroo.auth.port.`in`.LoggedInUserResult
import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.user.domain.MathStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

class AuthControllerTest {
    @Test
    fun `login returns bearer access token response`() {
        var capturedCommand: LoginUserCommand? = null
        val controller = AuthController(
            LoginUserUseCase { command ->
                capturedCommand = command
                LoggedInUserResult(
                    accessToken = "jwt-token",
                    tokenType = "Bearer",
                    expiresAt = Instant.parse("2026-04-29T01:00:00Z"),
                    user = LoggedInUser(
                        id = "user-1",
                        loginId = command.loginId,
                        nickname = "나루",
                        mathStatus = MathStatus.UNKNOWN,
                    ),
                )
            },
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
        assertEquals("jwt-token", response.body?.accessToken)
        assertEquals("Bearer", response.body?.tokenType)
        assertEquals("user-1", response.body?.user?.id)
    }
}

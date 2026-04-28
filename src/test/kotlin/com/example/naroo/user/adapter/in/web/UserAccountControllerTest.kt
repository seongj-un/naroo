package com.example.naroo.user.adapter.`in`.web

import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`in`.SignedUpUserResult
import com.example.naroo.user.port.`in`.SignUpUserCommand
import com.example.naroo.user.port.`in`.SignUpUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

class UserAccountControllerTest {
    @Test
    fun `sign-up returns created user response`() {
        var capturedCommand: SignUpUserCommand? = null
        val controller = UserAccountController(
            SignUpUserUseCase { command ->
                capturedCommand = command
                SignedUpUserResult(
                    id = UserId("user-1"),
                    loginId = command.loginId,
                    nickname = command.nickname,
                    mathStatus = command.mathStatus,
                    createdAt = Instant.parse("2026-04-29T00:00:00Z"),
                )
            },
        )

        val response = controller.signUp(
            SignUpUserRequest(
                loginId = "student01",
                password = "password123",
                nickname = "나루",
                mathStatus = MathStatus.MOSTLY_GAVE_UP,
            ),
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("student01", capturedCommand?.loginId)
        assertEquals("password123", capturedCommand?.password)
        assertEquals("나루", response.body?.nickname)
        assertEquals(MathStatus.MOSTLY_GAVE_UP, response.body?.mathStatus)
    }
}

package com.example.naroo.user.port.`in`

import com.example.naroo.user.domain.MathStatus
import java.time.Instant

fun interface LoginUserUseCase {
    fun login(command: LoginUserCommand): LoggedInUserResult
}

data class LoginUserCommand(
    val loginId: String,
    val password: String,
)

data class LoggedInUserResult(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
    val user: LoggedInUser,
)

data class LoggedInUser(
    val id: String,
    val loginId: String,
    val nickname: String,
    val mathStatus: MathStatus,
)

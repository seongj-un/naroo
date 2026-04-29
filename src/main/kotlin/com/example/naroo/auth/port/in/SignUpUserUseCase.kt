package com.example.naroo.auth.port.`in`

import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.UserId
import java.time.Instant

fun interface SignUpUserUseCase {
    fun signUp(command: SignUpUserCommand): SignedUpUserResult
}

data class SignUpUserCommand(
    val loginId: String,
    val password: String,
    val nickname: String,
    val mathStatus: MathStatus,
)

data class SignedUpUserResult(
    val id: UserId,
    val loginId: String,
    val nickname: String,
    val mathStatus: MathStatus,
    val createdAt: Instant,
)

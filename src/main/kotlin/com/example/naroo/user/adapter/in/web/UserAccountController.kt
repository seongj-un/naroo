package com.example.naroo.user.adapter.`in`.web

import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.port.`in`.SignUpUserCommand
import com.example.naroo.user.port.`in`.SignUpUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/users")
class UserAccountController(
    private val signUpUserUseCase: SignUpUserUseCase,
) {
    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpUserRequest): ResponseEntity<SignUpUserResponse> {
        val result = signUpUserUseCase.signUp(
            SignUpUserCommand(
                loginId = request.loginId,
                password = request.password,
                nickname = request.nickname,
                mathStatus = request.mathStatus,
            ),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SignUpUserResponse(
                id = result.id.value,
                loginId = result.loginId,
                nickname = result.nickname,
                mathStatus = result.mathStatus,
                createdAt = result.createdAt,
            ),
        )
    }

}

data class SignUpUserRequest(
    val loginId: String,
    val password: String,
    val nickname: String,
    val mathStatus: MathStatus = MathStatus.UNKNOWN,
)

data class SignUpUserResponse(
    val id: String,
    val loginId: String,
    val nickname: String,
    val mathStatus: MathStatus,
    val createdAt: Instant,
)

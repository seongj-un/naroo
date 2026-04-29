package com.example.naroo.user.adapter.`in`.web

import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.port.`in`.LoginUserCommand
import com.example.naroo.user.port.`in`.LoginUserUseCase
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
    private val loginUserUseCase: LoginUserUseCase,
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

    @PostMapping("/login")
    fun login(@RequestBody request: LoginUserRequest): ResponseEntity<LoginUserResponse> {
        val result = loginUserUseCase.login(
            LoginUserCommand(
                loginId = request.loginId,
                password = request.password,
            ),
        )

        return ResponseEntity.ok(
            LoginUserResponse(
                accessToken = result.accessToken,
                tokenType = result.tokenType,
                expiresAt = result.expiresAt,
                user = LoginUserResponseUser(
                    id = result.user.id,
                    loginId = result.user.loginId,
                    nickname = result.user.nickname,
                    mathStatus = result.user.mathStatus,
                ),
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

data class LoginUserRequest(
    val loginId: String,
    val password: String,
)

data class LoginUserResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
    val user: LoginUserResponseUser,
)

data class LoginUserResponseUser(
    val id: String,
    val loginId: String,
    val nickname: String,
    val mathStatus: MathStatus,
)

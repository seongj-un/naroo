package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.user.domain.MathStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val loginUserUseCase: LoginUserUseCase,
) {
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

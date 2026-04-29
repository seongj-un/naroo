package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.auth.port.`in`.ReissueTokenCommand
import com.example.naroo.auth.port.`in`.ReissueTokenUseCase
import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`in`.SignUpUserUseCase
import com.example.naroo.auth.port.`in`.VerifyEmailCommand
import com.example.naroo.auth.port.`in`.VerifyEmailUseCase
import com.example.naroo.user.domain.MathStatus
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val signUpUserUseCase: SignUpUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val reissueTokenUseCase: ReissueTokenUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
) {
    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpUserRequest): ResponseEntity<SignUpUserResponse> {
        val result = signUpUserUseCase.signUp(
            SignUpUserCommand(
                loginId = request.loginId,
                email = request.email,
                password = request.password,
                nickname = request.nickname,
                mathStatus = request.mathStatus,
            ),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SignUpUserResponse(
                id = result.id.value,
                loginId = result.loginId,
                email = result.email,
                emailVerified = result.emailVerified,
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

        return ResponseEntity.ok()
            .header("Set-Cookie", refreshTokenCookie(result.refreshToken, result.refreshTokenExpiresAt).toString())
            .body(
                LoginUserResponse(
                    accessToken = result.accessToken,
                    tokenType = result.tokenType,
                    expiresAt = result.expiresAt,
                    user = LoginUserResponseUser(
                        id = result.user.id,
                        loginId = result.user.loginId,
                        email = result.user.email,
                        emailVerified = result.user.emailVerified,
                        nickname = result.user.nickname,
                        mathStatus = result.user.mathStatus,
                    ),
                ),
            )
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) refreshToken: String?,
    ): ResponseEntity<ReissueTokenResponse> {
        if (refreshToken.isNullOrBlank()) {
            throw InvalidRefreshTokenRequestException()
        }

        val result = reissueTokenUseCase.reissue(ReissueTokenCommand(refreshToken = refreshToken))
        return ResponseEntity.ok()
            .header("Set-Cookie", refreshTokenCookie(result.refreshToken, result.refreshTokenExpiresAt).toString())
            .body(
                ReissueTokenResponse(
                    accessToken = result.accessToken,
                    tokenType = result.tokenType,
                    expiresAt = result.expiresAt,
                ),
            )
    }

    @PostMapping("/email/verify")
    fun verifyEmail(@RequestBody request: VerifyEmailRequest): ResponseEntity<VerifyEmailResponse> {
        val result = verifyEmailUseCase.verify(VerifyEmailCommand(token = request.token))
        return ResponseEntity.ok(
            VerifyEmailResponse(
                userId = result.userId,
                email = result.email,
                emailVerified = result.emailVerified,
            ),
        )
    }

    @GetMapping("/me")
    fun me(request: HttpServletRequest): ResponseEntity<MeResponse> {
        val authentication = request.getAttribute(JwtAuthentication.REQUEST_ATTRIBUTE) as JwtAuthentication
        return ResponseEntity.ok(
            MeResponse(
                id = authentication.userId,
                loginId = authentication.loginId,
                emailVerified = authentication.emailVerified,
                nickname = authentication.nickname,
            ),
        )
    }

    private fun refreshTokenCookie(refreshToken: String, expiresAt: Instant): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(java.time.Duration.between(Instant.now(), expiresAt).coerceAtLeast(java.time.Duration.ZERO))
            .build()
    }

    companion object {
        const val REFRESH_TOKEN_COOKIE = "refresh_token"
    }
}

data class SignUpUserRequest(
    val loginId: String,
    val email: String,
    val password: String,
    val nickname: String,
    val mathStatus: MathStatus = MathStatus.UNKNOWN,
)

data class SignUpUserResponse(
    val id: String,
    val loginId: String,
    val email: String,
    val emailVerified: Boolean,
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

data class ReissueTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
)

data class VerifyEmailRequest(
    val token: String,
)

data class VerifyEmailResponse(
    val userId: String,
    val email: String,
    val emailVerified: Boolean,
)

data class LoginUserResponseUser(
    val id: String,
    val loginId: String,
    val email: String,
    val emailVerified: Boolean,
    val nickname: String,
    val mathStatus: MathStatus,
)

data class MeResponse(
    val id: String,
    val loginId: String,
    val emailVerified: Boolean,
    val nickname: String,
)

class InvalidRefreshTokenRequestException : RuntimeException("refresh token is required")

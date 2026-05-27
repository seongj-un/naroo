package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.auth.port.`in`.ResendEmailVerificationCommand
import com.example.naroo.auth.port.`in`.ResendEmailVerificationUseCase
import com.example.naroo.auth.port.`in`.ReissueTokenCommand
import com.example.naroo.auth.port.`in`.ReissueTokenUseCase
import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`in`.SignUpUserUseCase
import com.example.naroo.auth.port.`in`.VerifyEmailCommand
import com.example.naroo.auth.port.`in`.VerifyEmailUseCase
import com.example.naroo.betaops.application.service.BusinessStageBetaEventTracker
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import com.example.naroo.user.domain.MathStatus
import org.springframework.beans.factory.annotation.Value
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
    private val resendEmailVerificationUseCase: ResendEmailVerificationUseCase,
    private val businessStageBetaEventTracker: BusinessStageBetaEventTracker,
    @Value("\${naroo.auth.refresh-cookie-secure:true}")
    private val refreshCookieSecure: Boolean = true,
    @Value("\${naroo.auth.refresh-cookie-same-site:Strict}")
    refreshCookieSameSite: String = "Strict",
) {
    private val validatedRefreshCookieSameSite = normalizeSameSite(refreshCookieSameSite)

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpUserRequest): ResponseEntity<APiWrappedResponseDto<SignUpUserResponse>> {
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
            ).toWrappedDto(),
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginUserRequest): ResponseEntity<APiWrappedResponseDto<LoginUserResponse>> {
        val result = loginUserUseCase.login(
            LoginUserCommand(
                loginId = request.loginId,
                password = request.password,
            ),
        )
        businessStageBetaEventTracker.loginSucceeded(result)

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
                        role = result.user.role,
                    ),
                ).toWrappedDto(),
            )
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) refreshToken: String?,
    ): ResponseEntity<APiWrappedResponseDto<ReissueTokenResponse>> {
        if (refreshToken.isNullOrBlank()) {
            throw AuthException.RefreshTokenRequired
        }

        val result = reissueTokenUseCase.reissue(ReissueTokenCommand(refreshToken = refreshToken))
        return ResponseEntity.ok()
            .header("Set-Cookie", refreshTokenCookie(result.refreshToken, result.refreshTokenExpiresAt).toString())
            .body(
                ReissueTokenResponse(
                    accessToken = result.accessToken,
                    tokenType = result.tokenType,
                    expiresAt = result.expiresAt,
                ).toWrappedDto(),
            )
    }

    @PostMapping("/email/verify")
    fun verifyEmail(@RequestBody request: VerifyEmailRequest): ResponseEntity<APiWrappedResponseDto<VerifyEmailResponse>> {
        val result = verifyEmailUseCase.verify(VerifyEmailCommand(token = request.token))
        return ResponseEntity.ok(
            VerifyEmailResponse(
                userId = result.userId,
                email = result.email,
                emailVerified = result.emailVerified,
            ).toWrappedDto(),
        )
    }

    @PostMapping("/email/resend")
    fun resendEmailVerification(): ResponseEntity<APiWrappedResponseDto<ResendEmailVerificationResponse>> {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        val result = resendEmailVerificationUseCase.resend(
            ResendEmailVerificationCommand(userId = authentication.userId),
        )
        return ResponseEntity.ok(
            ResendEmailVerificationResponse(
                userId = result.userId,
                email = result.email,
                emailVerified = result.emailVerified,
                nextRetryAt = result.nextRetryAt,
            ).toWrappedDto(),
        )
    }

    @GetMapping("/me")
    fun me(): APiWrappedResponseDto<MeResponse> {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        return (
            MeResponse(
                id = authentication.userId,
                loginId = authentication.loginId,
                emailVerified = authentication.emailVerified,
                nickname = authentication.nickname,
                role = authentication.role,
            ).toWrappedDto()
        )
    }

    private fun refreshTokenCookie(refreshToken: String, expiresAt: Instant): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .sameSite(validatedRefreshCookieSameSite)
            .path("/api/auth")
            .maxAge(java.time.Duration.between(Instant.now(), expiresAt).coerceAtLeast(java.time.Duration.ZERO))
            .build()
    }

    private fun normalizeSameSite(value: String): String {
        return when (value.trim().lowercase()) {
            "strict" -> "Strict"
            "lax" -> "Lax"
            "none" -> "None"
            else -> throw IllegalArgumentException(
                "naroo.auth.refresh-cookie-same-site must be one of Strict, Lax, None",
            )
        }
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
) : SuccessResponseDto

data class LoginUserRequest(
    val loginId: String,
    val password: String,
)

data class LoginUserResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
    val user: LoginUserResponseUser,
) : SuccessResponseDto

data class ReissueTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant,
) : SuccessResponseDto

data class VerifyEmailRequest(
    val token: String,
)

data class VerifyEmailResponse(
    val userId: String,
    val email: String,
    val emailVerified: Boolean,
) : SuccessResponseDto

data class ResendEmailVerificationResponse(
    val userId: String,
    val email: String,
    val emailVerified: Boolean,
    val nextRetryAt: Instant,
) : SuccessResponseDto

data class LoginUserResponseUser(
    val id: String,
    val loginId: String,
    val email: String,
    val emailVerified: Boolean,
    val nickname: String,
    val mathStatus: MathStatus,
    val role: String,
)

data class MeResponse(
    val id: String,
    val loginId: String,
    val emailVerified: Boolean,
    val nickname: String,
    val role: String,
) : SuccessResponseDto

package com.example.naroo.auth.adapter.`in`.web.errorCode

import com.example.naroo.auth.application.AuthException
import com.example.naroo.infrastructure.web.toWrappedDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object AuthExceptionMapper {
    fun toResponseEntity(ex: AuthException): ResponseEntity<Any> =
        when (ex) {
            is AuthException.LoginIdAlreadyExists ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(AuthErrorCode.LOGIN_ID_ALREADY_EXISTS.toWrappedDto())

            is AuthException.EmailAlreadyExists ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(AuthErrorCode.EMAIL_ALREADY_EXISTS.toWrappedDto())

            is AuthException.EmailAlreadyVerified ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(AuthErrorCode.EMAIL_ALREADY_VERIFIED.toWrappedDto())

            is AuthException.InvalidCredentials ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthErrorCode.INVALID_CREDENTIALS.toWrappedDto())

            is AuthException.InvalidRefreshToken ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthErrorCode.INVALID_REFRESH_TOKEN.toWrappedDto())

            is AuthException.InvalidEmailVerificationToken ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN.toWrappedDto())

            is AuthException.EmailVerificationResendTooSoon ->
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", ex.retryAfterSeconds.toString())
                    .body(AuthErrorCode.EMAIL_VERIFICATION_RESEND_TOO_SOON.toWrappedDto())

            is AuthException.RefreshTokenRequired ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthErrorCode.REFRESH_TOKEN_REQUIRED.toWrappedDto())

            is AuthException.Unauthorized ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthErrorCode.UNAUTHORIZED.toWrappedDto())
        }
}

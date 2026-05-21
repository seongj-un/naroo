package com.example.naroo.auth.application

import com.example.naroo.application.global.exception.UseCaseException

sealed class AuthException : UseCaseException() {
    data object LoginIdAlreadyExists : AuthException()

    data object EmailAlreadyExists : AuthException()

    data object EmailAlreadyVerified : AuthException()

    data object InvalidCredentials : AuthException()

    data object InvalidRefreshToken : AuthException()

    data object InvalidEmailVerificationToken : AuthException()

    data class EmailVerificationResendTooSoon(
        val retryAfterSeconds: Long,
    ) : AuthException()

    data object RefreshTokenRequired : AuthException()

    data object Unauthorized : AuthException()
}

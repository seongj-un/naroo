package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.application.service.DuplicateLoginIdException
import com.example.naroo.auth.application.service.DuplicateEmailException
import com.example.naroo.auth.application.service.InvalidEmailVerificationTokenException
import com.example.naroo.auth.application.service.InvalidLoginCredentialsException
import com.example.naroo.auth.application.service.InvalidRefreshTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(exception: IllegalArgumentException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            AuthApiErrorResponse(message = exception.message ?: "bad request"),
        )
    }

    @ExceptionHandler(DuplicateLoginIdException::class)
    fun handleDuplicateLoginId(exception: DuplicateLoginIdException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            AuthApiErrorResponse(message = exception.message ?: "loginId already exists"),
        )
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(exception: DuplicateEmailException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            AuthApiErrorResponse(message = exception.message ?: "email already exists"),
        )
    }

    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentials(exception: InvalidLoginCredentialsException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            AuthApiErrorResponse(message = exception.message ?: "invalid login credentials"),
        )
    }

    @ExceptionHandler(InvalidRefreshTokenException::class, InvalidRefreshTokenRequestException::class)
    fun handleInvalidRefreshToken(exception: RuntimeException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            AuthApiErrorResponse(message = exception.message ?: "invalid refresh token"),
        )
    }

    @ExceptionHandler(InvalidEmailVerificationTokenException::class)
    fun handleInvalidEmailVerificationToken(
        exception: InvalidEmailVerificationTokenException,
    ): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            AuthApiErrorResponse(message = exception.message ?: "invalid email verification token"),
        )
    }
}

data class AuthApiErrorResponse(
    val message: String,
)

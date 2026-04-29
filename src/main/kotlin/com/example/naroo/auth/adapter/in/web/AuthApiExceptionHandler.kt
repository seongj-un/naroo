package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.application.service.InvalidLoginCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthApiExceptionHandler {
    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentials(exception: InvalidLoginCredentialsException): ResponseEntity<AuthApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            AuthApiErrorResponse(message = exception.message ?: "invalid login credentials"),
        )
    }
}

data class AuthApiErrorResponse(
    val message: String,
)

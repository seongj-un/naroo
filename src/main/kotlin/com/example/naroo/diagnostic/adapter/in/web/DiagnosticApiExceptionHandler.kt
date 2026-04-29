package com.example.naroo.diagnostic.adapter.`in`.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DiagnosticApiExceptionHandler {
    @ExceptionHandler(EmailVerificationRequiredException::class)
    fun handleEmailVerificationRequired(
        exception: EmailVerificationRequiredException,
    ): ResponseEntity<DiagnosticApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            DiagnosticApiErrorResponse(message = exception.message ?: "email verification is required"),
        )
    }
}

data class DiagnosticApiErrorResponse(
    val message: String,
)

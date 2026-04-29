package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.diagnostic.application.service.StartingPointSelectionRequiredException
import com.example.naroo.diagnostic.application.service.DiagnosticQuestionsNotFoundException
import com.example.naroo.diagnostic.application.service.DiagnosticSessionNotFoundException
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

    @ExceptionHandler(StartingPointSelectionRequiredException::class)
    fun handleStartingPointSelectionRequired(
        exception: StartingPointSelectionRequiredException,
    ): ResponseEntity<DiagnosticApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            DiagnosticApiErrorResponse(message = exception.message ?: "starting point selection is required"),
        )
    }

    @ExceptionHandler(DiagnosticSessionNotFoundException::class, DiagnosticQuestionsNotFoundException::class)
    fun handleDiagnosticSessionNotFound(exception: RuntimeException): ResponseEntity<DiagnosticApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            DiagnosticApiErrorResponse(message = exception.message ?: "diagnostic session not found"),
        )
    }
}

data class DiagnosticApiErrorResponse(
    val message: String,
)

package com.example.naroo.user.adapter.`in`.web

import com.example.naroo.user.application.service.DuplicateLoginIdException
import com.example.naroo.user.application.service.InvalidLoginCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class UserApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(exception: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(message = exception.message ?: "bad request"),
        )
    }

    @ExceptionHandler(DuplicateLoginIdException::class)
    fun handleDuplicateLoginId(exception: DuplicateLoginIdException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiErrorResponse(message = exception.message ?: "loginId already exists"),
        )
    }

    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentials(exception: InvalidLoginCredentialsException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiErrorResponse(message = exception.message ?: "invalid login credentials"),
        )
    }
}

data class ApiErrorResponse(
    val message: String,
)

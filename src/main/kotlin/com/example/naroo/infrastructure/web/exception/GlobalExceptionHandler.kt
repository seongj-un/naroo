package com.example.naroo.infrastructure.web.exception

import com.example.naroo.application.global.exception.UseCaseException
import com.example.naroo.auth.adapter.`in`.web.errorCode.AuthExceptionMapper
import com.example.naroo.auth.application.AuthException
import com.example.naroo.diagnostic.adapter.`in`.web.errorCode.DiagnosticExceptionMapper
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.infrastructure.web.toWrappedDto
import com.example.naroo.recovery.adapter.`in`.web.errorCode.RecoveryMissionExceptionMapper
import com.example.naroo.recovery.application.RecoveryMissionException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(UseCaseException::class)
    fun handleUseCaseException(ex: UseCaseException): ResponseEntity<Any> =
        when (ex) {
            is AuthException -> AuthExceptionMapper.toResponseEntity(ex)
            is DiagnosticException -> DiagnosticExceptionMapper.toResponseEntity(ex)
            is RecoveryMissionException -> RecoveryMissionExceptionMapper.toResponseEntity(ex)
            else -> ResponseEntity.badRequest().body(GlobalErrorCode.BAD_REQUEST.toWrappedDto())
        }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Any> =
        ResponseEntity.badRequest().body(GlobalErrorCode.BAD_REQUEST.toWrappedDto())

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? =
        when {
            statusCode.is4xxClientError -> {
                ResponseEntity
                    .status(statusCode)
                    .body(mapInternalExceptionToErrorCode(ex).toWrappedDto())
            }

            statusCode.is5xxServerError -> {
                ResponseEntity.status(statusCode).build()
            }

            else -> {
                super.handleExceptionInternal(ex, body, headers, statusCode, request)
            }
        }

    private fun mapInternalExceptionToErrorCode(ex: Exception): GlobalErrorCode =
        when (ex) {
            is BindException, is TypeMismatchException -> GlobalErrorCode.VALIDATION_ERROR
            is HttpRequestMethodNotSupportedException -> GlobalErrorCode.METHOD_NOT_ALLOWED
            is NoHandlerFoundException -> GlobalErrorCode.NOT_FOUND
            else -> GlobalErrorCode.BAD_REQUEST
        }
}

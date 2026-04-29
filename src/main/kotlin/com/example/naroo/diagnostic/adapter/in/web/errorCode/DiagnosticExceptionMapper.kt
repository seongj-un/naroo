package com.example.naroo.diagnostic.adapter.`in`.web.errorCode

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.infrastructure.web.toWrappedDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object DiagnosticExceptionMapper {
    fun toResponseEntity(ex: DiagnosticException): ResponseEntity<Any> =
        when (ex) {
            is DiagnosticException.EmailVerificationRequired ->
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(DiagnosticErrorCode.EMAIL_VERIFICATION_REQUIRED.toWrappedDto())

            is DiagnosticException.StartingPointSelectionRequired ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(DiagnosticErrorCode.STARTING_POINT_SELECTION_REQUIRED.toWrappedDto())

            is DiagnosticException.DiagnosticSessionNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(DiagnosticErrorCode.DIAGNOSTIC_SESSION_NOT_FOUND.toWrappedDto())

            is DiagnosticException.DiagnosticQuestionsNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(DiagnosticErrorCode.DIAGNOSTIC_QUESTIONS_NOT_FOUND.toWrappedDto())
        }
}

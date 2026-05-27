package com.example.naroo.diagnostic.application

import com.example.naroo.application.global.exception.UseCaseException

sealed class DiagnosticException : UseCaseException() {
    data object EmailVerificationRequired : DiagnosticException()

    data object StartingPointSelectionRequired : DiagnosticException()

    data object DiagnosticSessionNotFound : DiagnosticException()

    data object DiagnosticQuestionsNotFound : DiagnosticException()

    data object InvalidDiagnosticAnswer : DiagnosticException()

    data object InvalidDiagnosticTelemetry : DiagnosticException()

    data object InvalidDiagnosticTrustFeedback : DiagnosticException()

    data object DiagnosticAlreadyCompleted : DiagnosticException()

    data object DiagnosticResultNotReady : DiagnosticException()
}

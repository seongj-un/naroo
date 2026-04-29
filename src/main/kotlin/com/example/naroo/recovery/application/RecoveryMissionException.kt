package com.example.naroo.recovery.application

import com.example.naroo.application.global.exception.UseCaseException

sealed class RecoveryMissionException : UseCaseException() {
    data object DiagnosticResultRequired : RecoveryMissionException()

    data object RecoveryMissionNotFound : RecoveryMissionException()

    data object RecoveryMissionAlreadyCompleted : RecoveryMissionException()

    data object InvalidRecoveryMissionSubmission : RecoveryMissionException()
}

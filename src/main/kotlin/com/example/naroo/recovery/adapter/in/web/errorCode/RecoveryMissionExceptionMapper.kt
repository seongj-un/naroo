package com.example.naroo.recovery.adapter.`in`.web.errorCode

import com.example.naroo.infrastructure.web.toWrappedDto
import com.example.naroo.recovery.application.RecoveryMissionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object RecoveryMissionExceptionMapper {
    fun toResponseEntity(ex: RecoveryMissionException): ResponseEntity<Any> =
        when (ex) {
            is RecoveryMissionException.DiagnosticResultRequired ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(RecoveryMissionErrorCode.DIAGNOSTIC_RESULT_REQUIRED.toWrappedDto())

            is RecoveryMissionException.RecoveryMissionNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(RecoveryMissionErrorCode.RECOVERY_MISSION_NOT_FOUND.toWrappedDto())

            is RecoveryMissionException.RecoveryMissionAlreadyCompleted ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(RecoveryMissionErrorCode.RECOVERY_MISSION_ALREADY_COMPLETED.toWrappedDto())

            is RecoveryMissionException.InvalidRecoveryMissionSubmission ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RecoveryMissionErrorCode.INVALID_RECOVERY_MISSION_SUBMISSION.toWrappedDto())

            is RecoveryMissionException.RecoveryMissionTemplateNotFound ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(RecoveryMissionErrorCode.RECOVERY_MISSION_TEMPLATE_NOT_FOUND.toWrappedDto())
        }
}

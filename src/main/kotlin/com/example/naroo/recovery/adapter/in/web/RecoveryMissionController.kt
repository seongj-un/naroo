package com.example.naroo.recovery.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CompleteRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.GetRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/recovery-missions")
class RecoveryMissionController(
    private val createRecoveryMissionUseCase: CreateRecoveryMissionUseCase,
    private val getRecoveryMissionUseCase: GetRecoveryMissionUseCase,
    private val completeRecoveryMissionUseCase: CompleteRecoveryMissionUseCase,
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateRecoveryMissionRequest,
    ): ResponseEntity<APiWrappedResponseDto<RecoveryMissionResponse>> {
        val authentication = verifiedAuthentication()
        val result = createRecoveryMissionUseCase.create(
            CreateRecoveryMissionCommand(
                userId = authentication.userId,
                diagnosticSessionId = request.diagnosticSessionId,
            ),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse().toWrappedDto())
    }

    @GetMapping("/{recoveryMissionId}")
    fun get(
        @PathVariable recoveryMissionId: String,
    ): APiWrappedResponseDto<RecoveryMissionResponse> {
        val authentication = verifiedAuthentication()
        val result = getRecoveryMissionUseCase.get(
            GetRecoveryMissionCommand(
                userId = authentication.userId,
                recoveryMissionId = recoveryMissionId,
            ),
        )

        return result.toResponse().toWrappedDto()
    }

    @PostMapping("/{recoveryMissionId}/complete")
    fun complete(
        @PathVariable recoveryMissionId: String,
    ): APiWrappedResponseDto<RecoveryMissionResponse> {
        val authentication = verifiedAuthentication()
        val result = completeRecoveryMissionUseCase.complete(
            CompleteRecoveryMissionCommand(
                userId = authentication.userId,
                recoveryMissionId = recoveryMissionId,
            ),
        )

        return result.toResponse().toWrappedDto()
    }

    private fun verifiedAuthentication(): JwtAuthentication {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        if (!authentication.emailVerified) {
            throw DiagnosticException.EmailVerificationRequired
        }
        return authentication
    }

    private fun RecoveryMissionResult.toResponse(): RecoveryMissionResponse {
        return RecoveryMissionResponse(
            id = id,
            diagnosticSessionId = diagnosticSessionId,
            conceptTag = conceptTag,
            title = title,
            prompt = prompt,
            hints = hints,
            status = status,
            estimatedMinutes = estimatedMinutes,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }
}

data class CreateRecoveryMissionRequest(
    val diagnosticSessionId: String,
)

data class RecoveryMissionResponse(
    val id: String,
    val diagnosticSessionId: String,
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val status: RecoveryMissionStatus,
    val estimatedMinutes: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
) : SuccessResponseDto

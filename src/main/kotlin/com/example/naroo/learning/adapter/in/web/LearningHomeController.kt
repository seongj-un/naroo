package com.example.naroo.learning.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import com.example.naroo.learning.port.`in`.GetLearningHomeCommand
import com.example.naroo.learning.port.`in`.GetLearningHomeUseCase
import com.example.naroo.learning.port.`in`.LearningHomeDiagnosticResult
import com.example.naroo.learning.port.`in`.LearningHomeMissionResult
import com.example.naroo.learning.port.`in`.LearningHomeNextAction
import com.example.naroo.learning.port.`in`.LearningHomeProgressResult
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/me/learning-home")
class LearningHomeController(
    private val getLearningHomeUseCase: GetLearningHomeUseCase,
) {
    @GetMapping
    fun get(): APiWrappedResponseDto<LearningHomeResponse> {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        val result = getLearningHomeUseCase.get(
            GetLearningHomeCommand(
                userId = authentication.userId,
                emailVerified = authentication.emailVerified,
            ),
        )

        return LearningHomeResponse(
            user = LearningHomeUserResponse(
                id = authentication.userId,
                nickname = authentication.nickname,
                emailVerified = authentication.emailVerified,
            ),
            nextAction = result.nextAction,
            latestDiagnostic = result.latestDiagnostic?.toResponse(),
            todayMission = result.todayMission?.toResponse(),
            latestMission = result.latestMission?.toResponse(),
            progress = result.progress.toResponse(),
        ).toWrappedDto()
    }
}

data class LearningHomeResponse(
    val user: LearningHomeUserResponse,
    val nextAction: LearningHomeNextAction,
    val latestDiagnostic: LearningHomeDiagnosticResponse?,
    val todayMission: LearningHomeMissionResponse?,
    val latestMission: LearningHomeMissionResponse?,
    val progress: LearningHomeProgressResponse,
) : SuccessResponseDto

data class LearningHomeUserResponse(
    val id: String,
    val nickname: String,
    val emailVerified: Boolean,
)

data class LearningHomeDiagnosticResponse(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
    val createdAt: Instant,
)

data class LearningHomeMissionResponse(
    val id: String,
    val diagnosticSessionId: String,
    val conceptTag: String,
    val title: String,
    val status: RecoveryMissionStatus,
    val estimatedMinutes: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
)

data class LearningHomeProgressResponse(
    val completedMissionCount: Long,
    val inProgressMissionCount: Long,
)

private fun LearningHomeDiagnosticResult.toResponse(): LearningHomeDiagnosticResponse {
    return LearningHomeDiagnosticResponse(
        diagnosticSessionId = diagnosticSessionId,
        mathArea = mathArea,
        status = status,
        totalQuestionCount = totalQuestionCount,
        correctCount = correctCount,
        wrongCount = wrongCount,
        unknownCount = unknownCount,
        weakLinks = weakLinks,
        primaryRecoveryConcept = primaryRecoveryConcept,
        summary = summary,
        createdAt = createdAt,
    )
}

private fun LearningHomeMissionResult.toResponse(): LearningHomeMissionResponse {
    return LearningHomeMissionResponse(
        id = id,
        diagnosticSessionId = diagnosticSessionId,
        conceptTag = conceptTag,
        title = title,
        status = status,
        estimatedMinutes = estimatedMinutes,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}

private fun LearningHomeProgressResult.toResponse(): LearningHomeProgressResponse {
    return LearningHomeProgressResponse(
        completedMissionCount = completedMissionCount,
        inProgressMissionCount = inProgressMissionCount,
    )
}

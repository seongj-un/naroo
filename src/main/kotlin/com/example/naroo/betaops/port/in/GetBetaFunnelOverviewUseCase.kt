package com.example.naroo.betaops.port.`in`

import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import java.time.Instant

fun interface GetBetaFunnelOverviewUseCase {
    fun get(): BetaFunnelOverviewResult
}

data class BetaFunnelOverviewResult(
    val loginUserCount: Int,
    val startingPointSelectedCount: Int,
    val diagnosticSessionCreatedCount: Int,
    val firstQuestionShownCount: Int,
    val firstAnswerSelectedCount: Int,
    val diagnosticSubmittedCount: Int,
    val trustFeedbackCount: Int,
    val trustFeelsRightCount: Int,
    val trustUnsureCount: Int,
    val recoveryMissionCreatedCount: Int,
    val recoveryMissionSubmittedCount: Int,
    val diagnosticSubmissionRate: Double,
    val trustFeedbackRate: Double,
    val recoveryMissionStartRate: Double,
    val recoveryMissionCompletionRate: Double,
    val sampleSize: Int,
    val lowSampleWarning: Boolean,
    val projectionLagSeconds: Long,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant?,
) : SuccessResponseDto

package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.port.`in`.BetaFunnelOverviewResult
import com.example.naroo.betaops.port.`in`.GetBetaFunnelOverviewUseCase
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class GetBetaFunnelOverviewService(
    private val refreshBetaFunnelProjectionService: RefreshBetaFunnelProjectionService,
) : GetBetaFunnelOverviewUseCase {
    override fun get(): BetaFunnelOverviewResult {
        val projection = refreshBetaFunnelProjectionService.refresh()
        val rows = projection.rows
        val loginUserCount = projection.sourceEvents
            .filter { it.eventType == BetaEventType.AUTH_LOGIN_SUCCEEDED }
            .map { it.userId }
            .distinct()
            .size

        val startingPointSelectedCount = rows.count { it.startingPointSelectedAt != null }
        val diagnosticSessionCreatedCount = rows.count { it.diagnosticSessionCreatedAt != null }
        val firstQuestionShownCount = rows.count { it.firstQuestionShownAt != null }
        val firstAnswerSelectedCount = rows.count { it.firstAnswerSelectedAt != null }
        val diagnosticSubmittedCount = rows.count { it.diagnosticSubmittedAt != null }
        val trustFeedbackCount = rows.count { it.trustFeedbackAt != null }
        val trustFeelsRightCount = rows.count { it.trustFeedbackChoice == DiagnosticResultTrustFeedbackChoice.FEELS_RIGHT }
        val trustUnsureCount = rows.count { it.trustFeedbackChoice == DiagnosticResultTrustFeedbackChoice.UNSURE }
        val recoveryMissionCreatedCount = rows.count { it.recoveryMissionCreatedAt != null }
        val recoveryMissionSubmittedCount = rows.count { it.recoveryMissionSubmittedAt != null }
        val sampleSize = diagnosticSessionCreatedCount

        return BetaFunnelOverviewResult(
            loginUserCount = loginUserCount,
            startingPointSelectedCount = startingPointSelectedCount,
            diagnosticSessionCreatedCount = diagnosticSessionCreatedCount,
            firstQuestionShownCount = firstQuestionShownCount,
            firstAnswerSelectedCount = firstAnswerSelectedCount,
            diagnosticSubmittedCount = diagnosticSubmittedCount,
            trustFeedbackCount = trustFeedbackCount,
            trustFeelsRightCount = trustFeelsRightCount,
            trustUnsureCount = trustUnsureCount,
            recoveryMissionCreatedCount = recoveryMissionCreatedCount,
            recoveryMissionSubmittedCount = recoveryMissionSubmittedCount,
            diagnosticSubmissionRate = rate(diagnosticSubmittedCount, diagnosticSessionCreatedCount),
            trustFeedbackRate = rate(trustFeedbackCount, diagnosticSubmittedCount),
            recoveryMissionStartRate = rate(recoveryMissionCreatedCount, diagnosticSubmittedCount),
            recoveryMissionCompletionRate = rate(recoveryMissionSubmittedCount, recoveryMissionCreatedCount),
            sampleSize = sampleSize,
            lowSampleWarning = sampleSize in 1..4,
            projectionLagSeconds = lagSeconds(projection),
            latestEventAt = projection.latestEventAt,
            lastProjectedEventAt = projection.lastProjectedEventAt,
            projectedAt = projection.projectedAt,
        )
    }

    private fun rate(numerator: Int, denominator: Int): Double {
        if (denominator == 0) {
            return 0.0
        }
        return round((numerator.toDouble() / denominator.toDouble()) * 1000) / 1000
    }

    private fun lagSeconds(projection: RefreshedBetaFunnelProjection): Long {
        val latestEventAt = projection.latestEventAt ?: return 0
        val lastProjectedEventAt = projection.lastProjectedEventAt ?: return 0
        return (latestEventAt.epochSecond - lastProjectedEventAt.epochSecond).coerceAtLeast(0)
    }
}

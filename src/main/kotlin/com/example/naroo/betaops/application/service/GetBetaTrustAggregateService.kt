package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaTrustAggregateRow
import com.example.naroo.betaops.port.`in`.BetaTrustAggregateResult
import com.example.naroo.betaops.port.`in`.BetaTrustAggregateRowResult
import com.example.naroo.betaops.port.`in`.GetBetaTrustAggregateUseCase
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class GetBetaTrustAggregateService(
    private val refreshBetaTrustAggregateProjectionService: RefreshBetaTrustAggregateProjectionService,
) : GetBetaTrustAggregateUseCase {
    override fun get(): BetaTrustAggregateResult {
        val projection = refreshBetaTrustAggregateProjectionService.refresh()
        return BetaTrustAggregateResult(
            rows = projection.rows.map { it.toResult() },
            rowCount = projection.rows.size,
            projectionLagSeconds = lagSeconds(projection),
            latestEventAt = projection.latestEventAt,
            lastProjectedEventAt = projection.lastProjectedEventAt,
            projectedAt = projection.projectedAt,
        )
    }

    private fun BetaTrustAggregateRow.toResult(): BetaTrustAggregateRowResult {
        return BetaTrustAggregateRowResult(
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            primaryRecoveryConcept = primaryRecoveryConcept,
            feedbackCount = feedbackCount,
            feelsRightCount = feelsRightCount,
            unsureCount = unsureCount,
            feelsRightRate = rate(feelsRightCount, feedbackCount),
            unsureRate = rate(unsureCount, feedbackCount),
            lowSampleWarning = feedbackCount in 1..4,
        )
    }

    private fun rate(numerator: Int, denominator: Int): Double {
        if (denominator == 0) {
            return 0.0
        }
        return round((numerator.toDouble() / denominator.toDouble()) * 1000) / 1000
    }

    private fun lagSeconds(projection: RefreshedBetaTrustAggregateProjection): Long {
        val latestEventAt = projection.latestEventAt ?: return 0
        val lastProjectedEventAt = projection.lastProjectedEventAt ?: return 0
        return (latestEventAt.epochSecond - lastProjectedEventAt.epochSecond).coerceAtLeast(0)
    }
}

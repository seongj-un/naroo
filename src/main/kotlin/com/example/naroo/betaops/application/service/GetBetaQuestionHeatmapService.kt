package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaQuestionHeatmapRow
import com.example.naroo.betaops.port.`in`.BetaQuestionHeatmapResult
import com.example.naroo.betaops.port.`in`.BetaQuestionHeatmapRowResult
import com.example.naroo.betaops.port.`in`.GetBetaQuestionHeatmapUseCase
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class GetBetaQuestionHeatmapService(
    private val refreshBetaQuestionHeatmapProjectionService: RefreshBetaQuestionHeatmapProjectionService,
) : GetBetaQuestionHeatmapUseCase {
    override fun get(): BetaQuestionHeatmapResult {
        val projection = refreshBetaQuestionHeatmapProjectionService.refresh()
        return BetaQuestionHeatmapResult(
            rows = projection.rows.map { it.toResult() },
            rowCount = projection.rows.size,
            projectionLagSeconds = lagSeconds(projection),
            latestEventAt = projection.latestEventAt,
            lastProjectedEventAt = projection.lastProjectedEventAt,
            projectedAt = projection.projectedAt,
        )
    }

    private fun BetaQuestionHeatmapRow.toResult(): BetaQuestionHeatmapRowResult {
        return BetaQuestionHeatmapRowResult(
            questionId = questionId,
            mathArea = mathArea,
            conceptTag = conceptTag,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            displayOrder = displayOrder,
            questionShownCount = questionShownCount,
            answerSelectedCount = answerSelectedCount,
            unknownAnswerCount = unknownAnswerCount,
            abandonedAfterQuestionCount = abandonedAfterQuestionCount,
            answerSelectionRate = rate(answerSelectedCount, questionShownCount),
            unknownSelectionRate = rate(unknownAnswerCount, questionShownCount),
            abandonmentRate = rate(abandonedAfterQuestionCount, questionShownCount),
            lowSampleWarning = questionShownCount in 1..4,
        )
    }

    private fun rate(numerator: Int, denominator: Int): Double {
        if (denominator == 0) {
            return 0.0
        }
        return round((numerator.toDouble() / denominator.toDouble()) * 1000) / 1000
    }

    private fun lagSeconds(projection: RefreshedBetaQuestionHeatmapProjection): Long {
        val latestEventAt = projection.latestEventAt ?: return 0
        val lastProjectedEventAt = projection.lastProjectedEventAt ?: return 0
        return (latestEventAt.epochSecond - lastProjectedEventAt.epochSecond).coerceAtLeast(0)
    }
}

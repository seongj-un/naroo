package com.example.naroo.betaops.port.`in`

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import java.time.Instant

fun interface GetBetaQuestionHeatmapUseCase {
    fun get(): BetaQuestionHeatmapResult
}

data class BetaQuestionHeatmapResult(
    val rows: List<BetaQuestionHeatmapRowResult>,
    val rowCount: Int,
    val projectionLagSeconds: Long,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant?,
) : SuccessResponseDto

data class BetaQuestionHeatmapRowResult(
    val questionId: String,
    val mathArea: MathArea?,
    val conceptTag: String?,
    val questionSnapshotVersion: Int?,
    val flowVariant: String?,
    val displayOrder: Int?,
    val questionShownCount: Int,
    val answerSelectedCount: Int,
    val unknownAnswerCount: Int,
    val abandonedAfterQuestionCount: Int,
    val answerSelectionRate: Double,
    val unknownSelectionRate: Double,
    val abandonmentRate: Double,
    val lowSampleWarning: Boolean,
)

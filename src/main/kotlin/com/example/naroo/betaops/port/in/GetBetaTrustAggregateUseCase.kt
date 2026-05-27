package com.example.naroo.betaops.port.`in`

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import java.time.Instant

fun interface GetBetaTrustAggregateUseCase {
    fun get(): BetaTrustAggregateResult
}

data class BetaTrustAggregateResult(
    val rows: List<BetaTrustAggregateRowResult>,
    val rowCount: Int,
    val projectionLagSeconds: Long,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant?,
) : SuccessResponseDto

data class BetaTrustAggregateRowResult(
    val mathArea: MathArea?,
    val questionSnapshotVersion: Int?,
    val flowVariant: String?,
    val resultCopyVersion: String?,
    val primaryRecoveryConcept: String?,
    val feedbackCount: Int,
    val feelsRightCount: Int,
    val unsureCount: Int,
    val feelsRightRate: Double,
    val unsureRate: Double,
    val lowSampleWarning: Boolean,
)

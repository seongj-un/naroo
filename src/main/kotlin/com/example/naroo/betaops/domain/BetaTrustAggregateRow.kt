package com.example.naroo.betaops.domain

import com.example.naroo.diagnostic.domain.MathArea
import java.time.Instant

data class BetaTrustAggregateRow(
    val id: String,
    val mathArea: MathArea? = null,
    val questionSnapshotVersion: Int? = null,
    val flowVariant: String? = null,
    val resultCopyVersion: String? = null,
    val primaryRecoveryConcept: String? = null,
    val feedbackCount: Int,
    val feelsRightCount: Int,
    val unsureCount: Int,
    val lastEventAt: Instant,
    val projectedAt: Instant,
)

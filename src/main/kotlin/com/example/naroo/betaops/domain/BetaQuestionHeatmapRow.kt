package com.example.naroo.betaops.domain

import com.example.naroo.diagnostic.domain.MathArea
import java.time.Instant

data class BetaQuestionHeatmapRow(
    val id: String,
    val questionId: String,
    val mathArea: MathArea? = null,
    val conceptTag: String? = null,
    val questionSnapshotVersion: Int? = null,
    val flowVariant: String? = null,
    val displayOrder: Int? = null,
    val questionShownCount: Int,
    val answerSelectedCount: Int,
    val unknownAnswerCount: Int,
    val abandonedAfterQuestionCount: Int,
    val lastEventAt: Instant,
    val projectedAt: Instant,
)

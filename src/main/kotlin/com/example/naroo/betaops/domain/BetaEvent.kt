package com.example.naroo.betaops.domain

import com.example.naroo.diagnostic.domain.MathArea
import java.time.Instant

data class BetaEvent(
    val id: String,
    val eventType: BetaEventType,
    val userId: String,
    val diagnosticSessionId: String? = null,
    val recoveryMissionId: String? = null,
    val questionId: String? = null,
    val mathArea: MathArea? = null,
    val questionSnapshotVersion: Int? = null,
    val flowVariant: String? = null,
    val resultCopyVersion: String? = null,
    val idempotencyKey: String,
    val payloadJson: String,
    val occurredAt: Instant,
    val receivedAt: Instant,
)

package com.example.naroo.betaops.domain

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import java.time.Instant

data class BetaFunnelRow(
    val diagnosticSessionId: String,
    val userId: String,
    val mathArea: MathArea? = null,
    val questionSnapshotVersion: Int? = null,
    val flowVariant: String? = null,
    val resultCopyVersion: String? = null,
    val loginSucceededAt: Instant? = null,
    val startingPointSelectedAt: Instant? = null,
    val diagnosticSessionCreatedAt: Instant? = null,
    val firstQuestionShownAt: Instant? = null,
    val firstAnswerSelectedAt: Instant? = null,
    val diagnosticSubmittedAt: Instant? = null,
    val trustFeedbackChoice: DiagnosticResultTrustFeedbackChoice? = null,
    val trustFeedbackAt: Instant? = null,
    val recoveryMissionCreatedAt: Instant? = null,
    val recoveryMissionSubmittedAt: Instant? = null,
    val lastEventType: BetaEventType,
    val lastEventAt: Instant,
    val projectedAt: Instant,
)

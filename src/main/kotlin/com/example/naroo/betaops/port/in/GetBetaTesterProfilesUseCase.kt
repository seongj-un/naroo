package com.example.naroo.betaops.port.`in`

import com.example.naroo.betaops.domain.BetaTesterTargetMatch
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.user.domain.UserRole
import java.time.Instant

fun interface GetBetaTesterProfilesUseCase {
    fun get(): BetaTesterProfilesResult
}

data class BetaTesterProfilesResult(
    val rows: List<BetaTesterProfileResult>,
    val rowCount: Int,
    val projectedAt: Instant?,
) : SuccessResponseDto

data class BetaTesterProfileResult(
    val userId: String,
    val loginId: String,
    val nickname: String,
    val role: UserRole,
    val cohortTag: String?,
    val targetMatch: BetaTesterTargetMatch?,
    val operatorNote: String?,
    val latestDiagnosticSessionId: String?,
    val latestMathArea: MathArea?,
    val latestFlowVariant: String?,
    val latestQuestionSnapshotVersion: Int?,
    val latestLastEventType: BetaEventType?,
    val latestLastEventAt: Instant?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) : SuccessResponseDto

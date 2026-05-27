package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaFunnelRow
import com.example.naroo.betaops.domain.BetaTesterProfile
import com.example.naroo.betaops.port.`in`.BetaTesterProfileResult
import com.example.naroo.betaops.port.`in`.BetaTesterProfilesResult
import com.example.naroo.betaops.port.`in`.GetBetaTesterProfilesUseCase
import com.example.naroo.betaops.port.`out`.BetaFunnelProjectionRepositoryPort
import com.example.naroo.betaops.port.`out`.BetaTesterProfileRepositoryPort
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GetBetaTesterProfilesService(
    private val refreshBetaFunnelProjectionService: RefreshBetaFunnelProjectionService,
    private val betaFunnelProjectionRepositoryPort: BetaFunnelProjectionRepositoryPort,
    private val betaTesterProfileRepositoryPort: BetaTesterProfileRepositoryPort,
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
) : GetBetaTesterProfilesUseCase {
    override fun get(): BetaTesterProfilesResult {
        val projection = refreshBetaFunnelProjectionService.refresh()
        val latestFunnelByUserId = betaFunnelProjectionRepositoryPort.findAll()
            .groupBy(BetaFunnelRow::userId)
            .mapValues { (_, rows) -> rows.maxBy { it.lastEventAt } }
        val profilesByUserId = betaTesterProfileRepositoryPort.findAll().associateBy(BetaTesterProfile::userId)
        val userIds = (latestFunnelByUserId.keys + profilesByUserId.keys).distinct()

        val rows = userIds.mapNotNull { userId ->
            val user = userAccountRepositoryPort.findById(UserId(userId)) ?: return@mapNotNull null
            val latestFunnelRow = latestFunnelByUserId[userId]
            val profile = profilesByUserId[userId]
            BetaTesterProfileResult(
                userId = user.id.value,
                loginId = user.loginId.value,
                nickname = user.nickname.value,
                role = user.role,
                cohortTag = profile?.cohortTag,
                targetMatch = profile?.targetMatch,
                operatorNote = profile?.operatorNote,
                latestDiagnosticSessionId = latestFunnelRow?.diagnosticSessionId,
                latestMathArea = latestFunnelRow?.mathArea,
                latestFlowVariant = latestFunnelRow?.flowVariant,
                latestQuestionSnapshotVersion = latestFunnelRow?.questionSnapshotVersion,
                latestLastEventType = latestFunnelRow?.lastEventType,
                latestLastEventAt = latestFunnelRow?.lastEventAt,
                createdAt = profile?.createdAt,
                updatedAt = profile?.updatedAt,
            )
        }.sortedWith(
            compareByDescending<BetaTesterProfileResult> { it.latestLastEventAt ?: it.updatedAt ?: Instant.EPOCH }
                .thenBy { it.loginId },
        )

        return BetaTesterProfilesResult(
            rows = rows,
            rowCount = rows.size,
            projectedAt = projection.projectedAt,
        )
    }
}

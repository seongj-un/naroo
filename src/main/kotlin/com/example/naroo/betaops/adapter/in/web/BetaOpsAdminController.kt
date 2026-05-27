package com.example.naroo.betaops.adapter.`in`.web

import com.example.naroo.betaops.port.`in`.BetaFunnelOverviewResult
import com.example.naroo.betaops.port.`in`.GetBetaFunnelOverviewUseCase
import com.example.naroo.betaops.port.`in`.BetaQuestionHeatmapResult
import com.example.naroo.betaops.port.`in`.GetBetaQuestionHeatmapUseCase
import com.example.naroo.betaops.domain.BetaTesterTargetMatch
import com.example.naroo.betaops.port.`in`.BetaTesterProfileResult
import com.example.naroo.betaops.port.`in`.BetaTesterProfilesResult
import com.example.naroo.betaops.port.`in`.BetaTrustAggregateResult
import com.example.naroo.betaops.port.`in`.GetBetaTesterProfilesUseCase
import com.example.naroo.betaops.port.`in`.GetBetaTrustAggregateUseCase
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileCommand
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileUseCase
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/beta-ops")
class BetaOpsAdminController(
    private val getBetaFunnelOverviewUseCase: GetBetaFunnelOverviewUseCase,
    private val getBetaQuestionHeatmapUseCase: GetBetaQuestionHeatmapUseCase,
    private val getBetaTrustAggregateUseCase: GetBetaTrustAggregateUseCase,
    private val getBetaTesterProfilesUseCase: GetBetaTesterProfilesUseCase,
    private val upsertBetaTesterProfileUseCase: UpsertBetaTesterProfileUseCase,
    private val betaOpsAdminAuthorizer: BetaOpsAdminAuthorizer,
) {
    @GetMapping("/funnel/overview")
    fun getFunnelOverview(): APiWrappedResponseDto<BetaFunnelOverviewResult> {
        betaOpsAdminAuthorizer.verify()
        return getBetaFunnelOverviewUseCase.get().toWrappedDto()
    }

    @GetMapping("/questions/heatmap")
    fun getQuestionHeatmap(): APiWrappedResponseDto<BetaQuestionHeatmapResult> {
        betaOpsAdminAuthorizer.verify()
        return getBetaQuestionHeatmapUseCase.get().toWrappedDto()
    }

    @GetMapping("/trust/aggregate")
    fun getTrustAggregate(): APiWrappedResponseDto<BetaTrustAggregateResult> {
        betaOpsAdminAuthorizer.verify()
        return getBetaTrustAggregateUseCase.get().toWrappedDto()
    }

    @GetMapping("/testers")
    fun getTesterProfiles(): APiWrappedResponseDto<BetaTesterProfilesResult> {
        betaOpsAdminAuthorizer.verify()
        return getBetaTesterProfilesUseCase.get().toWrappedDto()
    }

    @PutMapping("/testers/{userId}/profile")
    fun upsertTesterProfile(
        @PathVariable userId: String,
        @RequestBody request: UpsertBetaTesterProfileRequest,
    ): APiWrappedResponseDto<BetaTesterProfileResult> {
        betaOpsAdminAuthorizer.verify()
        return upsertBetaTesterProfileUseCase.upsert(
            UpsertBetaTesterProfileCommand(
                userId = userId,
                cohortTag = request.cohortTag,
                targetMatch = request.targetMatch,
                operatorNote = request.operatorNote,
            ),
        ).toWrappedDto()
    }
}

data class UpsertBetaTesterProfileRequest(
    val cohortTag: String? = null,
    val targetMatch: BetaTesterTargetMatch? = null,
    val operatorNote: String? = null,
)

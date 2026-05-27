package com.example.naroo.betaops.adapter.`in`.web

import com.example.naroo.betaops.port.`in`.BetaFunnelOverviewResult
import com.example.naroo.betaops.port.`in`.GetBetaFunnelOverviewUseCase
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/beta-ops")
class BetaOpsAdminController(
    private val getBetaFunnelOverviewUseCase: GetBetaFunnelOverviewUseCase,
    private val betaOpsAdminAuthorizer: BetaOpsAdminAuthorizer,
) {
    @GetMapping("/funnel/overview")
    fun getFunnelOverview(): APiWrappedResponseDto<BetaFunnelOverviewResult> {
        betaOpsAdminAuthorizer.verify()
        return getBetaFunnelOverviewUseCase.get().toWrappedDto()
    }
}

package com.example.naroo.betaops.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import com.example.naroo.betaops.port.`in`.BetaFunnelOverviewResult
import com.example.naroo.betaops.port.`in`.GetBetaFunnelOverviewUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

class BetaOpsAdminControllerTest {
    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns funnel overview for admin`() {
        val controller = BetaOpsAdminController(
            getBetaFunnelOverviewUseCase = GetBetaFunnelOverviewUseCase {
                BetaFunnelOverviewResult(
                    loginUserCount = 2,
                    startingPointSelectedCount = 2,
                    diagnosticSessionCreatedCount = 2,
                    firstQuestionShownCount = 2,
                    firstAnswerSelectedCount = 2,
                    diagnosticSubmittedCount = 1,
                    trustFeedbackCount = 1,
                    trustFeelsRightCount = 1,
                    trustUnsureCount = 0,
                    recoveryMissionCreatedCount = 1,
                    recoveryMissionSubmittedCount = 1,
                    diagnosticSubmissionRate = 0.5,
                    trustFeedbackRate = 1.0,
                    recoveryMissionStartRate = 1.0,
                    recoveryMissionCompletionRate = 1.0,
                    sampleSize = 2,
                    lowSampleWarning = true,
                    projectionLagSeconds = 0,
                    latestEventAt = Instant.parse("2026-05-27T06:08:00Z"),
                    lastProjectedEventAt = Instant.parse("2026-05-27T06:08:00Z"),
                    projectedAt = Instant.parse("2026-05-27T07:00:00Z"),
                )
            },
            betaOpsAdminAuthorizer = BetaOpsAdminAuthorizer(),
        )
        authenticate(role = "ADMIN")

        val response = controller.getFunnelOverview()

        assertEquals(2, response.data?.diagnosticSessionCreatedCount)
        assertEquals(1, response.data?.trustFeedbackCount)
    }

    @Test
    fun `rejects funnel overview without admin role`() {
        val controller = BetaOpsAdminController(
            getBetaFunnelOverviewUseCase = GetBetaFunnelOverviewUseCase {
                error("overview should not be called")
            },
            betaOpsAdminAuthorizer = BetaOpsAdminAuthorizer(),
        )
        authenticate(role = "STUDENT")

        assertThrows(AuthException.Unauthorized::class.java) {
            controller.getFunnelOverview()
        }
    }

    private fun authenticate(role: String) {
        val authentication = JwtAuthentication(
            tokenId = "token-1",
            userId = "user-1",
            loginId = "student01",
            emailVerified = true,
            nickname = "나루",
            role = role,
        )
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(authentication, null, emptyList())
    }
}

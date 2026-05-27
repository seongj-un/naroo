package com.example.naroo.betaops.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import com.example.naroo.betaops.domain.BetaTesterTargetMatch
import com.example.naroo.betaops.port.`in`.BetaFunnelOverviewResult
import com.example.naroo.betaops.port.`in`.BetaQuestionHeatmapResult
import com.example.naroo.betaops.port.`in`.BetaQuestionHeatmapRowResult
import com.example.naroo.betaops.port.`in`.BetaTesterProfileResult
import com.example.naroo.betaops.port.`in`.BetaTesterProfilesResult
import com.example.naroo.betaops.port.`in`.BetaTrustAggregateResult
import com.example.naroo.betaops.port.`in`.BetaTrustAggregateRowResult
import com.example.naroo.betaops.port.`in`.GetBetaFunnelOverviewUseCase
import com.example.naroo.betaops.port.`in`.GetBetaQuestionHeatmapUseCase
import com.example.naroo.betaops.port.`in`.GetBetaTesterProfilesUseCase
import com.example.naroo.betaops.port.`in`.GetBetaTrustAggregateUseCase
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileCommand
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileUseCase
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.user.domain.UserRole
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
        val controller = createController(
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
        )
        authenticate(role = "ADMIN")

        val response = controller.getFunnelOverview()

        assertEquals(2, response.data?.diagnosticSessionCreatedCount)
        assertEquals(1, response.data?.trustFeedbackCount)
    }

    @Test
    fun `rejects funnel overview without admin role`() {
        val controller = createController()
        authenticate(role = "STUDENT")

        assertThrows(AuthException.Unauthorized::class.java) {
            controller.getFunnelOverview()
        }
    }

    @Test
    fun `returns question heatmap for admin`() {
        val controller = createController(
            getBetaQuestionHeatmapUseCase = GetBetaQuestionHeatmapUseCase {
                BetaQuestionHeatmapResult(
                    rows = listOf(
                        BetaQuestionHeatmapRowResult(
                            questionId = "function-substitution-1",
                            mathArea = MathArea.FUNCTION,
                            conceptTag = "function_substitution",
                            questionSnapshotVersion = 3,
                            flowVariant = "beta-v1",
                            displayOrder = 1,
                            questionShownCount = 4,
                            answerSelectedCount = 3,
                            unknownAnswerCount = 1,
                            abandonedAfterQuestionCount = 1,
                            answerSelectionRate = 0.75,
                            unknownSelectionRate = 0.25,
                            abandonmentRate = 0.25,
                            lowSampleWarning = true,
                        ),
                    ),
                    rowCount = 1,
                    projectionLagSeconds = 0,
                    latestEventAt = Instant.parse("2026-05-27T07:08:00Z"),
                    lastProjectedEventAt = Instant.parse("2026-05-27T07:08:00Z"),
                    projectedAt = Instant.parse("2026-05-27T07:09:00Z"),
                )
            },
        )
        authenticate(role = "ADMIN")

        val response = controller.getQuestionHeatmap()

        assertEquals(1, response.data?.rowCount)
        assertEquals("function-substitution-1", response.data?.rows?.single()?.questionId)
        assertEquals(4, response.data?.rows?.single()?.questionShownCount)
    }

    @Test
    fun `returns trust aggregate for admin`() {
        val controller = createController(
            getBetaTrustAggregateUseCase = GetBetaTrustAggregateUseCase {
                BetaTrustAggregateResult(
                    rows = listOf(
                        BetaTrustAggregateRowResult(
                            mathArea = MathArea.FUNCTION,
                            questionSnapshotVersion = 3,
                            flowVariant = "beta-v1",
                            resultCopyVersion = "result-copy-v1",
                            primaryRecoveryConcept = "linear_function_slope",
                            feedbackCount = 2,
                            feelsRightCount = 1,
                            unsureCount = 1,
                            feelsRightRate = 0.5,
                            unsureRate = 0.5,
                            lowSampleWarning = true,
                        ),
                    ),
                    rowCount = 1,
                    projectionLagSeconds = 0,
                    latestEventAt = Instant.parse("2026-05-27T08:01:00Z"),
                    lastProjectedEventAt = Instant.parse("2026-05-27T08:01:00Z"),
                    projectedAt = Instant.parse("2026-05-27T08:02:00Z"),
                )
            },
        )
        authenticate(role = "ADMIN")

        val response = controller.getTrustAggregate()

        assertEquals(1, response.data?.rowCount)
        assertEquals(2, response.data?.rows?.single()?.feedbackCount)
        assertEquals(0.5, response.data?.rows?.single()?.feelsRightRate)
    }

    @Test
    fun `returns tester profiles for admin`() {
        val controller = createController(
            getBetaTesterProfilesUseCase = GetBetaTesterProfilesUseCase {
                BetaTesterProfilesResult(
                    rows = listOf(
                        BetaTesterProfileResult(
                            userId = "user-1",
                            loginId = "tester01",
                            nickname = "테스터",
                            role = UserRole.STUDENT,
                            cohortTag = "friend-intro",
                            targetMatch = BetaTesterTargetMatch.TARGET,
                            operatorNote = "Q1 이후 이탈",
                            latestDiagnosticSessionId = "diagnostic-session-1",
                            latestMathArea = MathArea.FUNCTION,
                            latestFlowVariant = "beta-v1",
                            latestQuestionSnapshotVersion = 3,
                            latestLastEventType = com.example.naroo.betaops.domain.BetaEventType.DIAGNOSTIC_SESSION_ABANDONED,
                            latestLastEventAt = Instant.parse("2026-05-27T08:05:00Z"),
                            createdAt = Instant.parse("2026-05-27T08:00:00Z"),
                            updatedAt = Instant.parse("2026-05-27T08:10:00Z"),
                        ),
                    ),
                    rowCount = 1,
                    projectedAt = Instant.parse("2026-05-27T08:10:00Z"),
                )
            },
        )
        authenticate(role = "ADMIN")

        val response = controller.getTesterProfiles()

        assertEquals(1, response.data?.rowCount)
        assertEquals("friend-intro", response.data?.rows?.single()?.cohortTag)
        assertEquals(BetaTesterTargetMatch.TARGET, response.data?.rows?.single()?.targetMatch)
    }

    @Test
    fun `upserts tester profile for admin`() {
        var capturedCommand: UpsertBetaTesterProfileCommand? = null
        val controller = createController(
            upsertBetaTesterProfileUseCase = UpsertBetaTesterProfileUseCase { command ->
                capturedCommand = command
                BetaTesterProfileResult(
                    userId = command.userId,
                    loginId = "tester01",
                    nickname = "테스터",
                    role = UserRole.STUDENT,
                    cohortTag = command.cohortTag,
                    targetMatch = command.targetMatch,
                    operatorNote = command.operatorNote,
                    latestDiagnosticSessionId = null,
                    latestMathArea = null,
                    latestFlowVariant = null,
                    latestQuestionSnapshotVersion = null,
                    latestLastEventType = null,
                    latestLastEventAt = null,
                    createdAt = Instant.parse("2026-05-27T08:00:00Z"),
                    updatedAt = Instant.parse("2026-05-27T08:10:00Z"),
                )
            },
        )
        authenticate(role = "ADMIN")

        val response = controller.upsertTesterProfile(
            userId = "user-1",
            request = UpsertBetaTesterProfileRequest(
                cohortTag = "friend-intro",
                targetMatch = BetaTesterTargetMatch.TARGET,
                operatorNote = "결과를 잘 믿는 편",
            ),
        )

        assertEquals("friend-intro", capturedCommand?.cohortTag)
        assertEquals(BetaTesterTargetMatch.TARGET, capturedCommand?.targetMatch)
        assertEquals("결과를 잘 믿는 편", capturedCommand?.operatorNote)
        assertEquals("friend-intro", response.data?.cohortTag)
    }

    private fun createController(
        getBetaFunnelOverviewUseCase: GetBetaFunnelOverviewUseCase = GetBetaFunnelOverviewUseCase {
            error("overview should not be called")
        },
        getBetaQuestionHeatmapUseCase: GetBetaQuestionHeatmapUseCase = GetBetaQuestionHeatmapUseCase {
            error("heatmap should not be called")
        },
        getBetaTrustAggregateUseCase: GetBetaTrustAggregateUseCase = GetBetaTrustAggregateUseCase {
            error("trust aggregate should not be called")
        },
        getBetaTesterProfilesUseCase: GetBetaTesterProfilesUseCase = GetBetaTesterProfilesUseCase {
            error("tester profiles should not be called")
        },
        upsertBetaTesterProfileUseCase: UpsertBetaTesterProfileUseCase = UpsertBetaTesterProfileUseCase {
            error("upsert tester profile should not be called")
        },
    ): BetaOpsAdminController {
        return BetaOpsAdminController(
            getBetaFunnelOverviewUseCase = getBetaFunnelOverviewUseCase,
            getBetaQuestionHeatmapUseCase = getBetaQuestionHeatmapUseCase,
            getBetaTrustAggregateUseCase = getBetaTrustAggregateUseCase,
            getBetaTesterProfilesUseCase = getBetaTesterProfilesUseCase,
            upsertBetaTesterProfileUseCase = upsertBetaTesterProfileUseCase,
            betaOpsAdminAuthorizer = BetaOpsAdminAuthorizer(),
        )
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

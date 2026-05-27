package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaFunnelRow
import com.example.naroo.betaops.domain.BetaTesterProfile
import com.example.naroo.betaops.domain.BetaTesterTargetMatch
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileCommand
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaFunnelProjectionRepositoryPort
import com.example.naroo.betaops.port.`out`.BetaTesterProfileRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.domain.UserRole
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BetaTesterProfileServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-27T10:00:00Z"), ZoneOffset.UTC)
    private val user = UserAccount(
        id = UserId("user-1"),
        loginId = LoginId.from("tester01"),
        email = EmailAddress.from("tester01@example.com"),
        emailVerified = true,
        passwordHash = PasswordHash("hashed-password"),
        nickname = Nickname.from("테스터"),
        mathStatus = MathStatus.UNKNOWN,
        createdAt = Instant.parse("2026-05-26T10:00:00Z"),
        role = UserRole.STUDENT,
    )

    @Test
    fun `upsert trims profile fields and returns saved profile`() {
        val repository = InMemoryBetaTesterProfileRepository()
        val service = UpsertBetaTesterProfileService(
            betaTesterProfileRepositoryPort = repository,
            userAccountRepositoryPort = FakeUserAccountRepository(user),
            clock = clock,
        )

        val result = service.upsert(
            UpsertBetaTesterProfileCommand(
                userId = user.id.value,
                cohortTag = " friend-intro ",
                targetMatch = BetaTesterTargetMatch.TARGET,
                operatorNote = "  로그인은 괜찮았고 결과를 믿는 편이었음.  ",
            ),
        )

        assertEquals("friend-intro", result.cohortTag)
        assertEquals(BetaTesterTargetMatch.TARGET, result.targetMatch)
        assertEquals("로그인은 괜찮았고 결과를 믿는 편이었음.", result.operatorNote)
        assertEquals(Instant.parse("2026-05-27T10:00:00Z"), repository.saved.single().updatedAt)
    }

    @Test
    fun `list merges saved profile with latest funnel row`() {
        val profileRepository = InMemoryBetaTesterProfileRepository()
        profileRepository.save(
            BetaTesterProfile(
                userId = user.id.value,
                cohortTag = "classmate",
                targetMatch = BetaTesterTargetMatch.PARTIAL,
                operatorNote = "Q1 이후 바로 이탈함",
                createdAt = Instant.parse("2026-05-27T09:00:00Z"),
                updatedAt = Instant.parse("2026-05-27T09:10:00Z"),
            ),
        )
        val funnelRepository = InMemoryBetaFunnelProjectionRepository()
        val refreshService = RefreshBetaFunnelProjectionService(
            betaEventReadPort = FakeBetaTesterProfileEventReadPort(events()),
            betaFunnelProjectionRepositoryPort = funnelRepository,
            objectMapper = ObjectMapper(),
            clock = clock,
        )
        val service = GetBetaTesterProfilesService(
            refreshBetaFunnelProjectionService = refreshService,
            betaFunnelProjectionRepositoryPort = funnelRepository,
            betaTesterProfileRepositoryPort = profileRepository,
            userAccountRepositoryPort = FakeUserAccountRepository(user),
        )

        val result = service.get()

        assertEquals(1, result.rowCount)
        val row = result.rows.single()
        assertEquals("tester01", row.loginId)
        assertEquals("classmate", row.cohortTag)
        assertEquals(BetaTesterTargetMatch.PARTIAL, row.targetMatch)
        assertEquals("diagnostic-session-1", row.latestDiagnosticSessionId)
        assertEquals(BetaEventType.DIAGNOSTIC_SESSION_ABANDONED, row.latestLastEventType)
        assertEquals(Instant.parse("2026-05-27T09:03:00Z"), row.latestLastEventAt)
        assertNull(row.latestFlowVariant)
    }

    private fun events(): List<BetaEvent> {
        return listOf(
            event(BetaEventType.AUTH_LOGIN_SUCCEEDED, occurredAt = "2026-05-27T09:00:00Z"),
            event(BetaEventType.DIAGNOSTIC_STARTING_POINT_SELECTED, occurredAt = "2026-05-27T09:01:00Z"),
            event(BetaEventType.DIAGNOSTIC_SESSION_CREATED, diagnosticSessionId = "diagnostic-session-1", occurredAt = "2026-05-27T09:02:00Z"),
            event(
                BetaEventType.DIAGNOSTIC_SESSION_ABANDONED,
                diagnosticSessionId = "diagnostic-session-1",
                questionId = "function-substitution-1",
                occurredAt = "2026-05-27T09:03:00Z",
            ),
        )
    }

    private fun event(
        eventType: BetaEventType,
        diagnosticSessionId: String? = null,
        questionId: String? = null,
        occurredAt: String,
    ): BetaEvent {
        return BetaEvent(
            id = "$eventType:$occurredAt",
            eventType = eventType,
            userId = user.id.value,
            diagnosticSessionId = diagnosticSessionId,
            questionId = questionId,
            mathArea = MathArea.FUNCTION,
            questionSnapshotVersion = 3,
            idempotencyKey = "$eventType:$occurredAt",
            payloadJson = "{}",
            occurredAt = Instant.parse(occurredAt),
            receivedAt = Instant.parse(occurredAt),
        )
    }
}

private class FakeUserAccountRepository(
    private val user: UserAccount,
) : UserAccountRepositoryPort {
    override fun existsByLoginId(loginId: LoginId): Boolean = loginId == user.loginId

    override fun existsByEmail(email: EmailAddress): Boolean = email == user.email

    override fun findByLoginId(loginId: LoginId): UserAccount? = user.takeIf { it.loginId == loginId }

    override fun findById(userId: UserId): UserAccount? = user.takeIf { it.id == userId }

    override fun save(userAccount: UserAccount): UserAccount = userAccount
}

private class InMemoryBetaTesterProfileRepository : BetaTesterProfileRepositoryPort {
    val saved = mutableListOf<BetaTesterProfile>()

    override fun findAll(): List<BetaTesterProfile> {
        return saved.sortedByDescending { it.updatedAt }
    }

    override fun findByUserId(userId: String): BetaTesterProfile? {
        return saved.find { it.userId == userId }
    }

    override fun save(profile: BetaTesterProfile): BetaTesterProfile {
        saved.removeIf { it.userId == profile.userId }
        saved += profile
        return profile
    }
}

private class InMemoryBetaFunnelProjectionRepository : BetaFunnelProjectionRepositoryPort {
    private var rows: List<BetaFunnelRow> = emptyList()

    override fun replaceAll(rows: List<BetaFunnelRow>): List<BetaFunnelRow> {
        this.rows = rows
        return rows
    }

    override fun findAll(): List<BetaFunnelRow> {
        return rows
    }
}

private class FakeBetaTesterProfileEventReadPort(
    private val events: List<BetaEvent>,
) : BetaEventReadPort {
    override fun findAllOrderByOccurredAtAscReceivedAtAsc(): List<BetaEvent> {
        return events
    }
}

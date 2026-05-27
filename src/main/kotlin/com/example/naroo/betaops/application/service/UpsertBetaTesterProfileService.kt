package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaTesterProfile
import com.example.naroo.betaops.port.`in`.BetaTesterProfileResult
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileCommand
import com.example.naroo.betaops.port.`in`.UpsertBetaTesterProfileUseCase
import com.example.naroo.betaops.port.`out`.BetaTesterProfileRepositoryPort
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class UpsertBetaTesterProfileService(
    private val betaTesterProfileRepositoryPort: BetaTesterProfileRepositoryPort,
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val clock: Clock,
) : UpsertBetaTesterProfileUseCase {
    override fun upsert(command: UpsertBetaTesterProfileCommand): BetaTesterProfileResult {
        val user = userAccountRepositoryPort.findById(UserId(command.userId))
            ?: throw IllegalArgumentException("beta tester profile user must exist")

        val existing = betaTesterProfileRepositoryPort.findByUserId(command.userId)
        val normalizedCohortTag = command.cohortTag?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedOperatorNote = command.operatorNote?.trim()?.takeIf { it.isNotEmpty() }
        require((normalizedCohortTag?.length ?: 0) <= 64) { "cohortTag must be 64 characters or fewer" }
        require((normalizedOperatorNote?.length ?: 0) <= 1000) { "operatorNote must be 1000 characters or fewer" }
        require(
            normalizedCohortTag != null || normalizedOperatorNote != null || command.targetMatch != null || existing != null,
        ) { "at least one tester profile field must be set" }

        val now = Instant.now(clock)
        val saved = betaTesterProfileRepositoryPort.save(
            BetaTesterProfile(
                userId = command.userId,
                cohortTag = normalizedCohortTag,
                targetMatch = command.targetMatch,
                operatorNote = normalizedOperatorNote,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )

        return BetaTesterProfileResult(
            userId = user.id.value,
            loginId = user.loginId.value,
            nickname = user.nickname.value,
            role = user.role,
            cohortTag = saved.cohortTag,
            targetMatch = saved.targetMatch,
            operatorNote = saved.operatorNote,
            latestDiagnosticSessionId = null,
            latestMathArea = null,
            latestFlowVariant = null,
            latestQuestionSnapshotVersion = null,
            latestLastEventType = null,
            latestLastEventAt = null,
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt,
        )
    }
}

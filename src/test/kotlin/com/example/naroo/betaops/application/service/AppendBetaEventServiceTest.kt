package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.example.naroo.betaops.port.`out`.DuplicateBetaEventIdempotencyKeyException
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AppendBetaEventServiceTest {
    private val objectMapper = ObjectMapper()
    private val clock = Clock.fixed(Instant.parse("2026-05-27T03:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `appends event when beta ops ingest is enabled`() {
        val repository = CapturingBetaEventRepository()
        val meterRegistry = SimpleMeterRegistry()
        val service = AppendBetaEventService(
            betaEventRepositoryPort = repository,
            objectMapper = objectMapper,
            meterRegistry = meterRegistry,
            clock = clock,
            modeValue = "shadow",
        )

        val outcome = service.append(validCommand())

        assertEquals(AppendBetaEventOutcome.APPENDED, outcome)
        assertEquals(1, repository.saved.size)
        assertEquals(1.0, meterRegistry.counter("beta_event_ingest_success_total", "event_type", BetaEventType.AUTH_LOGIN_SUCCEEDED.name).count())
        assertTrue(repository.saved.single().payloadJson.contains("\"role\":\"STUDENT\""))
    }

    @Test
    fun `returns duplicate when idempotency key was already seen`() {
        val meterRegistry = SimpleMeterRegistry()
        val service = AppendBetaEventService(
            betaEventRepositoryPort = DuplicateBetaEventRepository(),
            objectMapper = objectMapper,
            meterRegistry = meterRegistry,
            clock = clock,
            modeValue = "shadow",
        )

        val outcome = service.append(validCommand())

        assertEquals(AppendBetaEventOutcome.DUPLICATE, outcome)
        assertEquals(1.0, meterRegistry.counter("beta_event_duplicate_total", "event_type", BetaEventType.AUTH_LOGIN_SUCCEEDED.name).count())
    }

    @Test
    fun `skips ingest when beta ops mode is disabled`() {
        val repository = CapturingBetaEventRepository()
        val service = AppendBetaEventService(
            betaEventRepositoryPort = repository,
            objectMapper = objectMapper,
            meterRegistry = SimpleMeterRegistry(),
            clock = clock,
            modeValue = "disabled",
        )

        val outcome = service.append(validCommand())

        assertEquals(AppendBetaEventOutcome.SKIPPED, outcome)
        assertTrue(repository.saved.isEmpty())
    }

    private fun validCommand(): AppendBetaEventCommand {
        return AppendBetaEventCommand(
            eventType = BetaEventType.AUTH_LOGIN_SUCCEEDED,
            userId = "user-1",
            idempotencyKey = "auth-login-succeeded:user-1:2026-05-27T03:00:00Z",
            payload = mapOf(
                "role" to "STUDENT",
                "emailVerified" to true,
            ),
            occurredAt = Instant.parse("2026-05-27T03:00:00Z"),
        )
    }
}

private class CapturingBetaEventRepository : BetaEventRepositoryPort {
    val saved = mutableListOf<BetaEvent>()

    override fun save(event: BetaEvent): BetaEvent {
        saved += event
        return event
    }
}

private class DuplicateBetaEventRepository : BetaEventRepositoryPort {
    override fun save(event: BetaEvent): BetaEvent {
        throw DuplicateBetaEventIdempotencyKeyException(event.idempotencyKey)
    }
}

package com.example.naroo.support

import com.example.naroo.betaops.application.service.AppendBetaEventService
import com.example.naroo.betaops.application.service.BusinessStageBetaEventTracker
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

fun noOpBusinessStageBetaEventTracker(): BusinessStageBetaEventTracker {
    val clock = Clock.fixed(Instant.parse("2026-05-27T00:00:00Z"), ZoneOffset.UTC)
    return BusinessStageBetaEventTracker(
        appendBetaEventService = AppendBetaEventService(
            betaEventRepositoryPort = BetaEventRepositoryPort { event -> event },
            objectMapper = ObjectMapper(),
            meterRegistry = SimpleMeterRegistry(),
            clock = clock,
            modeValue = "disabled",
        ),
        clock = clock,
    )
}

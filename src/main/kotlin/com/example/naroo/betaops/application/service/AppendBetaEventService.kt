package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaOpsMode
import com.example.naroo.betaops.port.`out`.BetaEventRepositoryPort
import com.example.naroo.betaops.port.`out`.DuplicateBetaEventIdempotencyKeyException
import com.example.naroo.diagnostic.domain.MathArea
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class AppendBetaEventService(
    private val betaEventRepositoryPort: BetaEventRepositoryPort,
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val clock: Clock,
    @Value("\${naroo.beta-ops.mode:disabled}")
    modeValue: String,
) {
    private val mode = BetaOpsMode.from(modeValue)
    private val logger = LoggerFactory.getLogger(javaClass)

    fun append(command: AppendBetaEventCommand): AppendBetaEventOutcome {
        if (!mode.allowsEventIngest()) {
            return AppendBetaEventOutcome.SKIPPED
        }

        val startNanos = System.nanoTime()
        try {
            val payloadJson = objectMapper.writeValueAsString(command.payload)
            betaEventRepositoryPort.save(
                BetaEvent(
                    id = UUID.randomUUID().toString(),
                    eventType = command.eventType,
                    userId = command.userId,
                    diagnosticSessionId = command.diagnosticSessionId,
                    recoveryMissionId = command.recoveryMissionId,
                    questionId = command.questionId,
                    mathArea = command.mathArea,
                    questionSnapshotVersion = command.questionSnapshotVersion,
                    flowVariant = command.flowVariant,
                    resultCopyVersion = command.resultCopyVersion,
                    idempotencyKey = command.idempotencyKey,
                    payloadJson = payloadJson,
                    occurredAt = command.occurredAt,
                    receivedAt = Instant.now(clock),
                ),
            )
            meterRegistry.counter(
                "beta_event_ingest_success_total",
                "event_type",
                command.eventType.name,
            ).increment()
            return AppendBetaEventOutcome.APPENDED
        } catch (exception: DuplicateBetaEventIdempotencyKeyException) {
            meterRegistry.counter(
                "beta_event_duplicate_total",
                "event_type",
                command.eventType.name,
            ).increment()
            logger.info(
                "beta_event_duplicate eventType={} userId={} idempotencyKey={}",
                command.eventType,
                command.userId,
                command.idempotencyKey,
            )
            return AppendBetaEventOutcome.DUPLICATE
        } catch (exception: Exception) {
            meterRegistry.counter(
                "beta_event_ingest_failure_total",
                "event_type",
                command.eventType.name,
                "failure_kind",
                exception.javaClass.simpleName,
            ).increment()
            logger.warn(
                "beta_event_ingest_failure eventType={} userId={} diagnosticSessionId={} recoveryMissionId={} idempotencyKey={} failureKind={}",
                command.eventType,
                command.userId,
                command.diagnosticSessionId,
                command.recoveryMissionId,
                command.idempotencyKey,
                exception.javaClass.simpleName,
                exception,
            )
            throw exception
        } finally {
            meterRegistry.timer(
                "beta_event_ingest_latency",
                "event_type",
                command.eventType.name,
            ).record(
                System.nanoTime() - startNanos,
                java.util.concurrent.TimeUnit.NANOSECONDS,
            )
        }
    }
}

data class AppendBetaEventCommand(
    val eventType: BetaEventType,
    val userId: String,
    val idempotencyKey: String,
    val payload: Map<String, Any?>,
    val occurredAt: Instant,
    val diagnosticSessionId: String? = null,
    val recoveryMissionId: String? = null,
    val questionId: String? = null,
    val mathArea: MathArea? = null,
    val questionSnapshotVersion: Int? = null,
    val flowVariant: String? = null,
    val resultCopyVersion: String? = null,
)

enum class AppendBetaEventOutcome {
    APPENDED,
    DUPLICATE,
    SKIPPED,
}

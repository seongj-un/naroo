package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaTrustAggregateRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaTrustAggregateProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultTrustFeedbackChoice
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class RefreshBetaTrustAggregateProjectionService(
    private val betaEventReadPort: BetaEventReadPort,
    private val betaTrustAggregateProjectionRepositoryPort: BetaTrustAggregateProjectionRepositoryPort,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {
    fun refresh(): RefreshedBetaTrustAggregateProjection {
        val events = betaEventReadPort.findAllOrderByOccurredAtAscReceivedAtAsc()
        val projectedAt = Instant.now(clock)
        val rowsById = linkedMapOf<String, MutableBetaTrustAggregateRow>()

        events.forEach { event ->
            if (event.eventType != BetaEventType.DIAGNOSTIC_RESULT_TRUST_FEEDBACK_SUBMITTED) {
                return@forEach
            }

            val payload = parsePayload(event.payloadJson)
            val rowId = buildRowId(
                mathArea = event.mathArea,
                questionSnapshotVersion = event.questionSnapshotVersion,
                flowVariant = event.flowVariant,
                resultCopyVersion = event.resultCopyVersion,
                primaryRecoveryConcept = payload.string("primaryRecoveryConcept"),
            )
            val row = rowsById.getOrPut(rowId) {
                MutableBetaTrustAggregateRow(
                    id = rowId,
                    mathArea = event.mathArea,
                    questionSnapshotVersion = event.questionSnapshotVersion,
                    flowVariant = event.flowVariant,
                    resultCopyVersion = event.resultCopyVersion,
                    primaryRecoveryConcept = payload.string("primaryRecoveryConcept"),
                    lastEventAt = event.occurredAt,
                )
            }
            row.apply(event, payload)
        }

        val rows = rowsById.values.map { it.toRow(projectedAt) }
            .sortedWith(
                compareBy<BetaTrustAggregateRow> { it.mathArea?.name ?: "" }
                    .thenBy { it.questionSnapshotVersion ?: Int.MAX_VALUE }
                    .thenBy { it.flowVariant ?: "" }
                    .thenBy { it.resultCopyVersion ?: "" }
                    .thenBy { it.primaryRecoveryConcept ?: "" },
            )
        betaTrustAggregateProjectionRepositoryPort.replaceAll(rows)

        val latestEventAt = events.maxOfOrNull { it.occurredAt }
        val lastProjectedEventAt = rows.maxOfOrNull { it.lastEventAt }
        return RefreshedBetaTrustAggregateProjection(
            sourceEvents = events,
            rows = rows,
            latestEventAt = latestEventAt,
            lastProjectedEventAt = lastProjectedEventAt,
            projectedAt = projectedAt,
        )
    }

    private fun MutableBetaTrustAggregateRow.apply(event: BetaEvent, payload: JsonNode?) {
        mathArea = event.mathArea ?: mathArea
        questionSnapshotVersion = event.questionSnapshotVersion ?: questionSnapshotVersion
        flowVariant = event.flowVariant ?: flowVariant
        resultCopyVersion = event.resultCopyVersion ?: resultCopyVersion
        primaryRecoveryConcept = payload.string("primaryRecoveryConcept") ?: primaryRecoveryConcept

        feedbackCount += 1
        when (payload.string("feedbackChoice")?.let(::parseFeedbackChoice)) {
            DiagnosticResultTrustFeedbackChoice.FEELS_RIGHT -> feelsRightCount += 1
            DiagnosticResultTrustFeedbackChoice.UNSURE -> unsureCount += 1
            null -> {
                // no-op
            }
        }

        if (event.occurredAt >= lastEventAt) {
            lastEventAt = event.occurredAt
        }
    }

    private fun parseFeedbackChoice(rawValue: String): DiagnosticResultTrustFeedbackChoice? {
        return runCatching { DiagnosticResultTrustFeedbackChoice.valueOf(rawValue) }.getOrNull()
    }

    private fun buildRowId(
        mathArea: MathArea?,
        questionSnapshotVersion: Int?,
        flowVariant: String?,
        resultCopyVersion: String?,
        primaryRecoveryConcept: String?,
    ): String {
        return listOf(
            mathArea?.name ?: "none",
            questionSnapshotVersion?.toString() ?: "none",
            flowVariant ?: "none",
            resultCopyVersion ?: "none",
            primaryRecoveryConcept ?: "none",
        ).joinToString("|")
    }

    private fun parsePayload(payloadJson: String): JsonNode? {
        return runCatching { objectMapper.readTree(payloadJson) }.getOrNull()
    }

    private fun JsonNode?.string(fieldName: String): String? {
        return this?.path(fieldName)?.takeIf { !it.isMissingNode && !it.isNull }?.asText()?.takeIf { it.isNotBlank() }
    }
}

data class RefreshedBetaTrustAggregateProjection(
    val sourceEvents: List<BetaEvent>,
    val rows: List<BetaTrustAggregateRow>,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant,
)

private data class MutableBetaTrustAggregateRow(
    val id: String,
    var mathArea: MathArea? = null,
    var questionSnapshotVersion: Int? = null,
    var flowVariant: String? = null,
    var resultCopyVersion: String? = null,
    var primaryRecoveryConcept: String? = null,
    var feedbackCount: Int = 0,
    var feelsRightCount: Int = 0,
    var unsureCount: Int = 0,
    var lastEventAt: Instant,
) {
    fun toRow(projectedAt: Instant): BetaTrustAggregateRow {
        return BetaTrustAggregateRow(
            id = id,
            mathArea = mathArea,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            resultCopyVersion = resultCopyVersion,
            primaryRecoveryConcept = primaryRecoveryConcept,
            feedbackCount = feedbackCount,
            feelsRightCount = feelsRightCount,
            unsureCount = unsureCount,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }
}

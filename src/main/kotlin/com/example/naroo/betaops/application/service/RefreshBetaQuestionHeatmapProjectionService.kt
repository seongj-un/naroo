package com.example.naroo.betaops.application.service

import com.example.naroo.betaops.domain.BetaEvent
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.betaops.domain.BetaQuestionHeatmapRow
import com.example.naroo.betaops.port.`out`.BetaEventReadPort
import com.example.naroo.betaops.port.`out`.BetaQuestionHeatmapProjectionRepositoryPort
import com.example.naroo.diagnostic.domain.MathArea
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class RefreshBetaQuestionHeatmapProjectionService(
    private val betaEventReadPort: BetaEventReadPort,
    private val betaQuestionHeatmapProjectionRepositoryPort: BetaQuestionHeatmapProjectionRepositoryPort,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {
    fun refresh(): RefreshedBetaQuestionHeatmapProjection {
        val events = betaEventReadPort.findAllOrderByOccurredAtAscReceivedAtAsc()
        val projectedAt = Instant.now(clock)
        val rowsById = linkedMapOf<String, MutableBetaQuestionHeatmapRow>()

        events.forEach { event ->
            if (!event.eventType.isHeatmapEventType()) {
                return@forEach
            }

            val questionId = event.questionId ?: return@forEach
            val payload = parsePayload(event.payloadJson)
            val rowId = buildRowId(
                questionId = questionId,
                questionSnapshotVersion = event.questionSnapshotVersion,
                flowVariant = event.flowVariant,
                mathArea = event.mathArea,
            )
            val row = rowsById.getOrPut(rowId) {
                MutableBetaQuestionHeatmapRow(
                    id = rowId,
                    questionId = questionId,
                    mathArea = event.mathArea,
                    conceptTag = payload.string("conceptTag"),
                    questionSnapshotVersion = event.questionSnapshotVersion,
                    flowVariant = event.flowVariant,
                    displayOrder = payload.int("questionDisplayOrder"),
                    lastEventAt = event.occurredAt,
                )
            }
            row.apply(event, payload)
        }

        val rows = rowsById.values.map { it.toRow(projectedAt) }
            .sortedWith(compareBy<BetaQuestionHeatmapRow> { it.mathArea?.name ?: "" }.thenBy { it.displayOrder ?: Int.MAX_VALUE }.thenBy { it.questionId })
        betaQuestionHeatmapProjectionRepositoryPort.replaceAll(rows)

        val latestEventAt = events.maxOfOrNull { it.occurredAt }
        val lastProjectedEventAt = rows.maxOfOrNull { it.lastEventAt }
        return RefreshedBetaQuestionHeatmapProjection(
            sourceEvents = events,
            rows = rows,
            latestEventAt = latestEventAt,
            lastProjectedEventAt = lastProjectedEventAt,
            projectedAt = projectedAt,
        )
    }

    private fun MutableBetaQuestionHeatmapRow.apply(event: BetaEvent, payload: JsonNode?) {
        mathArea = event.mathArea ?: mathArea
        conceptTag = payload.string("conceptTag") ?: conceptTag
        questionSnapshotVersion = event.questionSnapshotVersion ?: questionSnapshotVersion
        flowVariant = event.flowVariant ?: flowVariant
        displayOrder = payload.int("questionDisplayOrder") ?: displayOrder

        when (event.eventType) {
            BetaEventType.DIAGNOSTIC_QUESTION_SHOWN -> questionShownCount += 1
            BetaEventType.DIAGNOSTIC_ANSWER_SELECTED -> {
                answerSelectedCount += 1
                if (payload.boolean("selectedUnknown") == true) {
                    unknownAnswerCount += 1
                }
            }

            BetaEventType.DIAGNOSTIC_SESSION_ABANDONED -> abandonedAfterQuestionCount += 1
            else -> {
                // no-op
            }
        }

        if (event.occurredAt >= lastEventAt) {
            lastEventAt = event.occurredAt
        }
    }

    private fun buildRowId(
        questionId: String,
        questionSnapshotVersion: Int?,
        flowVariant: String?,
        mathArea: MathArea?,
    ): String {
        return listOf(
            questionId,
            questionSnapshotVersion?.toString() ?: "none",
            flowVariant ?: "none",
            mathArea?.name ?: "none",
        ).joinToString("|")
    }

    private fun parsePayload(payloadJson: String): JsonNode? {
        return runCatching { objectMapper.readTree(payloadJson) }.getOrNull()
    }

    private fun JsonNode?.string(fieldName: String): String? {
        return this?.path(fieldName)?.takeIf { !it.isMissingNode && !it.isNull }?.asText()?.takeIf { it.isNotBlank() }
    }

    private fun JsonNode?.int(fieldName: String): Int? {
        return this?.path(fieldName)?.takeIf { !it.isMissingNode && !it.isNull }?.asInt()
    }

    private fun JsonNode?.boolean(fieldName: String): Boolean? {
        return this?.path(fieldName)?.takeIf { !it.isMissingNode && !it.isNull }?.asBoolean()
    }

    private fun BetaEventType.isHeatmapEventType(): Boolean {
        return this == BetaEventType.DIAGNOSTIC_QUESTION_SHOWN ||
            this == BetaEventType.DIAGNOSTIC_ANSWER_SELECTED ||
            this == BetaEventType.DIAGNOSTIC_SESSION_ABANDONED
    }
}

data class RefreshedBetaQuestionHeatmapProjection(
    val sourceEvents: List<BetaEvent>,
    val rows: List<BetaQuestionHeatmapRow>,
    val latestEventAt: Instant?,
    val lastProjectedEventAt: Instant?,
    val projectedAt: Instant,
)

private data class MutableBetaQuestionHeatmapRow(
    val id: String,
    val questionId: String,
    var mathArea: MathArea? = null,
    var conceptTag: String? = null,
    var questionSnapshotVersion: Int? = null,
    var flowVariant: String? = null,
    var displayOrder: Int? = null,
    var questionShownCount: Int = 0,
    var answerSelectedCount: Int = 0,
    var unknownAnswerCount: Int = 0,
    var abandonedAfterQuestionCount: Int = 0,
    var lastEventAt: Instant,
) {
    fun toRow(projectedAt: Instant): BetaQuestionHeatmapRow {
        return BetaQuestionHeatmapRow(
            id = id,
            questionId = questionId,
            mathArea = mathArea,
            conceptTag = conceptTag,
            questionSnapshotVersion = questionSnapshotVersion,
            flowVariant = flowVariant,
            displayOrder = displayOrder,
            questionShownCount = questionShownCount,
            answerSelectedCount = answerSelectedCount,
            unknownAnswerCount = unknownAnswerCount,
            abandonedAfterQuestionCount = abandonedAfterQuestionCount,
            lastEventAt = lastEventAt,
            projectedAt = projectedAt,
        )
    }
}

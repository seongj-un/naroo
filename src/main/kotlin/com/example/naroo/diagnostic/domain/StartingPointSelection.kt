package com.example.naroo.diagnostic.domain

import com.example.naroo.user.domain.UserId
import java.time.Instant

data class StartingPointSelection(
    val id: StartingPointSelectionId,
    val userId: UserId,
    val selectionType: StartingPointSelectionType,
    val mathArea: MathArea,
    val note: StartingPointNote?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
value class StartingPointSelectionId(val value: String) {
    init {
        require(value.isNotBlank()) { "startingPointSelectionId must not be blank" }
    }
}

enum class StartingPointSelectionType {
    WEAK_AREA,
    STUDY_INTEREST,
}

enum class MathArea {
    EQUATION,
    FUNCTION,
    GEOMETRY,
    PROBABILITY_AND_STATISTICS,
    SEQUENCE,
}

@JvmInline
value class StartingPointNote private constructor(val value: String) {
    companion object {
        fun fromNullable(value: String?): StartingPointNote? {
            val normalized = value?.trim().orEmpty()
            if (normalized.isBlank()) {
                return null
            }
            require(normalized.length <= 200) { "note must be 200 characters or fewer" }
            return StartingPointNote(normalized)
        }
    }
}

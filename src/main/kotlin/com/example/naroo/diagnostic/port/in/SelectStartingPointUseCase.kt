package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import java.time.Instant

fun interface SelectStartingPointUseCase {
    fun select(command: SelectStartingPointCommand): SelectedStartingPointResult
}

data class SelectStartingPointCommand(
    val userId: String,
    val selectionType: StartingPointSelectionType,
    val mathArea: MathArea,
    val note: String?,
)

data class SelectedStartingPointResult(
    val id: String,
    val userId: String,
    val selectionType: StartingPointSelectionType,
    val mathArea: MathArea,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

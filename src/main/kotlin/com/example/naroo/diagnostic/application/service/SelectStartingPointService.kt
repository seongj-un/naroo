package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.domain.StartingPointNote
import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import com.example.naroo.diagnostic.port.`in`.SelectedStartingPointResult
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionIdGeneratorPort
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SelectStartingPointService(
    private val startingPointSelectionRepositoryPort: StartingPointSelectionRepositoryPort,
    private val startingPointSelectionIdGeneratorPort: StartingPointSelectionIdGeneratorPort,
    private val clock: Clock,
) : SelectStartingPointUseCase {
    override fun select(command: SelectStartingPointCommand): SelectedStartingPointResult {
        val userId = UserId(command.userId)
        val note = StartingPointNote.fromNullable(command.note)
        val now = Instant.now(clock)
        val existingSelection = startingPointSelectionRepositoryPort.findByUserId(userId)

        val selection = if (existingSelection == null) {
            StartingPointSelection(
                id = startingPointSelectionIdGeneratorPort.generate(),
                userId = userId,
                selectionType = command.selectionType,
                mathArea = command.mathArea,
                note = note,
                createdAt = now,
                updatedAt = now,
            )
        } else {
            existingSelection.copy(
                selectionType = command.selectionType,
                mathArea = command.mathArea,
                note = note,
                updatedAt = now,
            )
        }

        return startingPointSelectionRepositoryPort.save(selection).toResult()
    }

    private fun StartingPointSelection.toResult(): SelectedStartingPointResult {
        return SelectedStartingPointResult(
            id = id.value,
            userId = userId.value,
            selectionType = selectionType,
            mathArea = mathArea,
            note = note?.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

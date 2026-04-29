package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionIdGeneratorPort
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SelectStartingPointServiceTest {
    private val repository = FakeStartingPointSelectionRepository()
    private val service = SelectStartingPointService(
        startingPointSelectionRepositoryPort = repository,
        startingPointSelectionIdGeneratorPort = StartingPointSelectionIdGeneratorPort {
            StartingPointSelectionId("starting-point-1")
        },
        clock = Clock.fixed(Instant.parse("2026-04-29T00:00:00Z"), ZoneOffset.UTC),
    )

    @Test
    fun `selects a new starting point`() {
        val result = service.select(
            SelectStartingPointCommand(
                userId = "user-1",
                selectionType = StartingPointSelectionType.WEAK_AREA,
                mathArea = MathArea.FUNCTION,
                note = "함수가 제일 헷갈려요",
            ),
        )

        assertEquals("starting-point-1", result.id)
        assertEquals("user-1", result.userId)
        assertEquals(StartingPointSelectionType.WEAK_AREA, result.selectionType)
        assertEquals(MathArea.FUNCTION, result.mathArea)
        assertEquals("함수가 제일 헷갈려요", result.note)
        assertEquals(Instant.parse("2026-04-29T00:00:00Z"), result.createdAt)
        assertEquals(Instant.parse("2026-04-29T00:00:00Z"), result.updatedAt)
    }

    @Test
    fun `updates existing starting point for same user`() {
        service.select(
            SelectStartingPointCommand(
                userId = "user-1",
                selectionType = StartingPointSelectionType.WEAK_AREA,
                mathArea = MathArea.FUNCTION,
                note = "처음 선택",
            ),
        )

        val result = service.select(
            SelectStartingPointCommand(
                userId = "user-1",
                selectionType = StartingPointSelectionType.STUDY_INTEREST,
                mathArea = MathArea.SEQUENCE,
                note = " ",
            ),
        )

        assertEquals("starting-point-1", result.id)
        assertEquals(StartingPointSelectionType.STUDY_INTEREST, result.selectionType)
        assertEquals(MathArea.SEQUENCE, result.mathArea)
        assertNull(result.note)
        assertEquals(1, repository.saved.size)
    }

    @Test
    fun `rejects long note`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.select(
                SelectStartingPointCommand(
                    userId = "user-1",
                    selectionType = StartingPointSelectionType.WEAK_AREA,
                    mathArea = MathArea.FUNCTION,
                    note = "a".repeat(201),
                ),
            )
        }
    }
}

private class FakeStartingPointSelectionRepository : StartingPointSelectionRepositoryPort {
    val saved = mutableMapOf<UserId, StartingPointSelection>()

    override fun findByUserId(userId: UserId): StartingPointSelection? {
        return saved[userId]
    }

    override fun save(selection: StartingPointSelection): StartingPointSelection {
        saved[selection.userId] = selection
        return selection
    }
}

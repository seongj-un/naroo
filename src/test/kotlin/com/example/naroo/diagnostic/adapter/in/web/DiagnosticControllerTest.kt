package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import com.example.naroo.diagnostic.port.`in`.SelectedStartingPointResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import java.time.Instant

class DiagnosticControllerTest {
    @Test
    fun `select starting point returns created response for verified user`() {
        var capturedCommand: SelectStartingPointCommand? = null
        val controller = DiagnosticController(
            SelectStartingPointUseCase { command ->
                capturedCommand = command
                SelectedStartingPointResult(
                    id = "starting-point-1",
                    userId = command.userId,
                    selectionType = command.selectionType,
                    mathArea = command.mathArea,
                    note = command.note?.trim(),
                    createdAt = Instant.parse("2026-04-29T00:00:00Z"),
                    updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
                )
            },
        )
        val httpRequest = authenticatedRequest(emailVerified = true)

        val response = controller.selectStartingPoint(
            httpRequest = httpRequest,
            request = SelectStartingPointRequest(
                selectionType = StartingPointSelectionType.STUDY_INTEREST,
                mathArea = MathArea.SEQUENCE,
                note = "수열을 다시 해보고 싶어요",
            ),
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("user-1", capturedCommand?.userId)
        assertEquals(StartingPointSelectionType.STUDY_INTEREST, capturedCommand?.selectionType)
        assertEquals(MathArea.SEQUENCE, capturedCommand?.mathArea)
        assertEquals("수열을 다시 해보고 싶어요", response.body?.note)
    }

    @Test
    fun `select starting point requires verified email`() {
        val controller = DiagnosticController(
            SelectStartingPointUseCase { error("use case should not be called") },
        )

        assertThrows(EmailVerificationRequiredException::class.java) {
            controller.selectStartingPoint(
                httpRequest = authenticatedRequest(emailVerified = false),
                request = SelectStartingPointRequest(
                    selectionType = StartingPointSelectionType.WEAK_AREA,
                    mathArea = MathArea.FUNCTION,
                    note = null,
                ),
            )
        }
    }

    private fun authenticatedRequest(emailVerified: Boolean): MockHttpServletRequest {
        return MockHttpServletRequest("POST", "/api/diagnostics/starting-point").apply {
            setAttribute(
                JwtAuthentication.REQUEST_ATTRIBUTE,
                JwtAuthentication(
                    tokenId = "token-1",
                    userId = "user-1",
                    loginId = "student01",
                    emailVerified = emailVerified,
                    nickname = "나루",
                ),
            )
        }
    }
}

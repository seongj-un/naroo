package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultCommand
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class GetDiagnosticResultServiceTest {
    @Test
    fun `returns completed result with next mission preview`() {
        val service = GetDiagnosticResultService(
            diagnosticSessionRepositoryPort = FakeResultDiagnosticSessionRepository(
                diagnosticSession().copy(status = DiagnosticSessionStatus.COMPLETED),
            ),
            diagnosticResultRepositoryPort = FakeDiagnosticResultRepository(diagnosticResult()),
        )

        val result = service.get(
            GetDiagnosticResultCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals(DiagnosticSessionStatus.COMPLETED, result.status)
        assertEquals(listOf("linear_function_slope"), result.weakLinks)
        assertEquals("linear_function_slope", result.nextMissionPreview.conceptTag)
        assertEquals(10, result.nextMissionPreview.estimatedMinutes)
    }

    @Test
    fun `rejects result before diagnostic is completed`() {
        val service = GetDiagnosticResultService(
            diagnosticSessionRepositoryPort = FakeResultDiagnosticSessionRepository(diagnosticSession()),
            diagnosticResultRepositoryPort = FakeDiagnosticResultRepository(null),
        )

        assertThrows(DiagnosticException.DiagnosticResultNotReady::class.java) {
            service.get(
                GetDiagnosticResultCommand(
                    userId = "user-1",
                    diagnosticSessionId = "diagnostic-session-1",
                ),
            )
        }
    }

    private fun diagnosticSession(): DiagnosticSession {
        return DiagnosticSession(
            id = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            startingPointSelectionId = StartingPointSelectionId("starting-point-1"),
            mathArea = MathArea.FUNCTION,
            status = DiagnosticSessionStatus.IN_PROGRESS,
            createdAt = Instant.parse("2026-04-29T00:00:00Z"),
            updatedAt = Instant.parse("2026-04-29T00:00:00Z"),
        )
    }

    private fun diagnosticResult(): DiagnosticResult {
        return DiagnosticResult(
            diagnosticSessionId = DiagnosticSessionId("diagnostic-session-1"),
            userId = UserId("user-1"),
            mathArea = MathArea.FUNCTION,
            totalQuestionCount = 2,
            correctCount = 1,
            wrongCount = 0,
            unknownCount = 1,
            weakLinks = listOf("linear_function_slope"),
            primaryRecoveryConcept = "linear_function_slope",
            summary = "전체가 무너진 게 아니에요.",
            createdAt = Instant.parse("2026-04-29T02:00:00Z"),
        )
    }
}

private class FakeResultDiagnosticSessionRepository(
    private val session: DiagnosticSession?,
) : DiagnosticSessionRepositoryPort {
    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return session?.takeIf { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        error("session should not be saved")
    }
}

private class FakeDiagnosticResultRepository(
    private val result: DiagnosticResult?,
) : DiagnosticResultRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return result?.takeIf { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        error("result should not be saved")
    }
}

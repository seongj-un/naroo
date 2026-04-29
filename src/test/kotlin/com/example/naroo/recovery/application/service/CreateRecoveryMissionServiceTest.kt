package com.example.naroo.recovery.application.service

import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionCommand
import com.example.naroo.recovery.port.`out`.RecoveryMissionIdGeneratorPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import com.example.naroo.user.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CreateRecoveryMissionServiceTest {
    @Test
    fun `creates first recovery mission from completed diagnostic result`() {
        val repository = CapturingRecoveryMissionRepository()
        val service = CreateRecoveryMissionService(
            diagnosticSessionRepositoryPort = FakeRecoveryDiagnosticSessionRepository(
                diagnosticSession().copy(status = DiagnosticSessionStatus.COMPLETED),
            ),
            diagnosticResultRepositoryPort = FakeRecoveryDiagnosticResultRepository(diagnosticResult()),
            recoveryMissionRepositoryPort = repository,
            recoveryMissionTemplateRepositoryPort = FakeRecoveryMissionTemplateRepository(recoveryMissionTemplates()),
            recoveryMissionIdGeneratorPort = RecoveryMissionIdGeneratorPort { RecoveryMissionId("mission-1") },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.create(
            CreateRecoveryMissionCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals("mission-1", result.id)
        assertEquals("linear_function_slope", result.conceptTag)
        assertEquals("일차함수 기울기 10분 복구 미션", result.title)
        assertEquals(RecoveryMissionStatus.IN_PROGRESS, result.status)
        assertEquals(10, result.estimatedMinutes)
        assertEquals(3, result.hints.size)
        assertEquals(result.id, repository.saved.single().id.value)
    }

    @Test
    fun `returns existing recovery mission for the same diagnostic session`() {
        val existingMission = recoveryMission()
        val repository = CapturingRecoveryMissionRepository(existingMission)
        val service = CreateRecoveryMissionService(
            diagnosticSessionRepositoryPort = FakeRecoveryDiagnosticSessionRepository(
                diagnosticSession().copy(status = DiagnosticSessionStatus.COMPLETED),
            ),
            diagnosticResultRepositoryPort = FakeRecoveryDiagnosticResultRepository(diagnosticResult()),
            recoveryMissionRepositoryPort = repository,
            recoveryMissionTemplateRepositoryPort = FakeRecoveryMissionTemplateRepository(recoveryMissionTemplates()),
            recoveryMissionIdGeneratorPort = RecoveryMissionIdGeneratorPort { error("id should not be generated") },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.create(
            CreateRecoveryMissionCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals(existingMission.id.value, result.id)
        assertEquals(true, repository.saved.isEmpty())
    }

    @Test
    fun `creates next weak link mission after previous concept is completed`() {
        val existingMission = recoveryMission().copy(
            conceptTag = "linear_function_slope",
            status = RecoveryMissionStatus.COMPLETED,
            completedAt = Instant.parse("2026-04-29T03:10:00Z"),
        )
        val repository = CapturingRecoveryMissionRepository(existingMission)
        val service = CreateRecoveryMissionService(
            diagnosticSessionRepositoryPort = FakeRecoveryDiagnosticSessionRepository(
                diagnosticSession().copy(status = DiagnosticSessionStatus.COMPLETED),
            ),
            diagnosticResultRepositoryPort = FakeRecoveryDiagnosticResultRepository(
                diagnosticResult().copy(
                    weakLinks = listOf("linear_function_slope", "function_substitution"),
                    primaryRecoveryConcept = "linear_function_slope",
                ),
            ),
            recoveryMissionRepositoryPort = repository,
            recoveryMissionTemplateRepositoryPort = FakeRecoveryMissionTemplateRepository(recoveryMissionTemplates()),
            recoveryMissionIdGeneratorPort = RecoveryMissionIdGeneratorPort { RecoveryMissionId("mission-2") },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:20:00Z"), ZoneOffset.UTC),
        )

        val result = service.create(
            CreateRecoveryMissionCommand(
                userId = "user-1",
                diagnosticSessionId = "diagnostic-session-1",
            ),
        )

        assertEquals("mission-2", result.id)
        assertEquals("function_substitution", result.conceptTag)
        assertEquals("함수값 대입 10분 복구 미션", result.title)
        assertEquals("function_substitution", repository.saved.single().conceptTag)
    }

    @Test
    fun `rejects mission creation before result exists`() {
        val service = CreateRecoveryMissionService(
            diagnosticSessionRepositoryPort = FakeRecoveryDiagnosticSessionRepository(diagnosticSession()),
            diagnosticResultRepositoryPort = FakeRecoveryDiagnosticResultRepository(null),
            recoveryMissionRepositoryPort = CapturingRecoveryMissionRepository(),
            recoveryMissionTemplateRepositoryPort = FakeRecoveryMissionTemplateRepository(recoveryMissionTemplates()),
            recoveryMissionIdGeneratorPort = RecoveryMissionIdGeneratorPort { RecoveryMissionId("mission-1") },
            clock = Clock.fixed(Instant.parse("2026-04-29T03:00:00Z"), ZoneOffset.UTC),
        )

        assertThrows(RecoveryMissionException.DiagnosticResultRequired::class.java) {
            service.create(
                CreateRecoveryMissionCommand(
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

    private fun recoveryMission(): RecoveryMission {
        return RecoveryMission(
            id = RecoveryMissionId("mission-1"),
            userId = UserId("user-1"),
            diagnosticSessionId = DiagnosticSessionId("diagnostic-session-1"),
            conceptTag = "linear_function_slope",
            title = "일차함수 기울기 10분 복구 미션",
            prompt = "y = ax + b에서 기울기가 어떤 숫자인지 찾는 연습만 해요.",
            hints = listOf("x 앞에 붙은 숫자를 먼저 찾아봐요."),
            status = RecoveryMissionStatus.IN_PROGRESS,
            estimatedMinutes = 10,
            createdAt = Instant.parse("2026-04-29T03:00:00Z"),
            completedAt = null,
        )
    }

    private fun recoveryMissionTemplates(): List<RecoveryMissionTemplate> {
        return listOf(
            RecoveryMissionTemplate(
                conceptTag = "linear_function_slope",
                title = "일차함수 기울기 10분 복구 미션",
                prompt = "y = ax + b에서 기울기가 어떤 숫자인지 찾는 연습만 해요.",
                hints = listOf(
                    "x 앞에 붙은 숫자를 먼저 찾아봐요.",
                    "부호도 같이 봐야 해요.",
                    "상수항은 시작 높이일 뿐이에요.",
                ),
                estimatedMinutes = 10,
            ),
            RecoveryMissionTemplate(
                conceptTag = "function_substitution",
                title = "함수값 대입 10분 복구 미션",
                prompt = "식에 x값을 넣고 y값이 어떻게 바뀌는지 한 줄씩 확인해요.",
                hints = listOf(
                    "x가 들어간 자리에 주어진 숫자만 먼저 넣어봐요.",
                    "곱셈과 덧셈 순서를 나눠서 한 줄씩 적어봐요.",
                    "계산 결과가 y값이에요.",
                ),
                estimatedMinutes = 10,
            ),
        )
    }
}

private class FakeRecoveryDiagnosticSessionRepository(
    private val session: DiagnosticSession?,
) : DiagnosticSessionRepositoryPort {
    override fun findById(id: DiagnosticSessionId): DiagnosticSession? {
        return session?.takeIf { it.id == id }
    }

    override fun save(session: DiagnosticSession): DiagnosticSession {
        error("session should not be saved")
    }
}

private class FakeRecoveryDiagnosticResultRepository(
    private val result: DiagnosticResult?,
) : DiagnosticResultRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return result?.takeIf { it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findLatestByUserId(userId: UserId): DiagnosticResult? {
        return result?.takeIf { it.userId == userId }
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        error("result should not be saved")
    }
}

private class CapturingRecoveryMissionRepository(
    private var mission: RecoveryMission? = null,
) : RecoveryMissionRepositoryPort {
    val saved = mutableListOf<RecoveryMission>()

    override fun findById(id: RecoveryMissionId): RecoveryMission? {
        return mission?.takeIf { it.id == id }
    }

    override fun findByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): RecoveryMission? {
        return mission?.takeIf { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId }
    }

    override fun findAllByUserIdAndDiagnosticSessionId(
        userId: UserId,
        diagnosticSessionId: DiagnosticSessionId,
    ): List<RecoveryMission> {
        return listOfNotNull(mission?.takeIf { it.userId == userId && it.diagnosticSessionId == diagnosticSessionId })
    }

    override fun findLatestInProgressByUserId(userId: UserId): RecoveryMission? {
        return mission?.takeIf { it.userId == userId && it.status == RecoveryMissionStatus.IN_PROGRESS }
    }

    override fun countByUserIdAndStatus(userId: UserId, status: RecoveryMissionStatus): Long {
        return listOfNotNull(mission).count { it.userId == userId && it.status == status }.toLong()
    }

    override fun save(mission: RecoveryMission): RecoveryMission {
        this.mission = mission
        saved += mission
        return mission
    }
}

private class FakeRecoveryMissionTemplateRepository(
    private val templates: List<RecoveryMissionTemplate>,
) : RecoveryMissionTemplateRepositoryPort {
    override fun findByConceptTag(conceptTag: String): RecoveryMissionTemplate? {
        return templates.firstOrNull { it.conceptTag == conceptTag }
    }

    override fun findAll(): List<RecoveryMissionTemplate> {
        return templates.sortedBy { it.conceptTag }
    }
}

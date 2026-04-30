package com.example.naroo.recovery.application.service

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.CreateRecoveryMissionUseCase
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import com.example.naroo.recovery.port.`out`.RecoveryMissionIdGeneratorPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class CreateRecoveryMissionService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticResultRepositoryPort: DiagnosticResultRepositoryPort,
    private val recoveryMissionRepositoryPort: RecoveryMissionRepositoryPort,
    private val recoveryMissionTemplateRepositoryPort: RecoveryMissionTemplateRepositoryPort,
    private val recoveryMissionIdGeneratorPort: RecoveryMissionIdGeneratorPort,
    private val clock: Clock,
) : CreateRecoveryMissionUseCase {
    override fun create(command: CreateRecoveryMissionCommand): RecoveryMissionResult {
        val userId = UserId(command.userId)
        val diagnosticSessionId = DiagnosticSessionId(command.diagnosticSessionId)
        val session = diagnosticSessionRepositoryPort.findById(diagnosticSessionId)
            ?.takeIf { it.userId == userId && it.status == DiagnosticSessionStatus.COMPLETED }
            ?: throw RecoveryMissionException.DiagnosticResultRequired

        val result = diagnosticResultRepositoryPort.findByDiagnosticSessionId(session.id)
            ?: throw RecoveryMissionException.DiagnosticResultRequired
        val existingMissions = recoveryMissionRepositoryPort.findAllByUserIdAndDiagnosticSessionId(userId, diagnosticSessionId)
        existingMissions.firstOrNull { it.status == RecoveryMissionStatus.IN_PROGRESS }?.let {
            return it.toResult()
        }

        val nextConcept = result.nextRecoveryConceptAfter(existingMissions.map { it.conceptTag }.toSet())
            ?: return existingMissions.lastOrNull()?.toResult()
                ?: throw RecoveryMissionException.DiagnosticResultRequired
        val template = recoveryMissionTemplateRepositoryPort.findActiveByConceptTag(nextConcept)
            ?: throw RecoveryMissionException.RecoveryMissionTemplateNotFound
        val now = Instant.now(clock)
        val mission = RecoveryMission(
            id = recoveryMissionIdGeneratorPort.generate(),
            userId = userId,
            diagnosticSessionId = diagnosticSessionId,
            conceptTag = nextConcept,
            title = template.title,
            prompt = template.prompt,
            hints = template.hints,
            status = RecoveryMissionStatus.IN_PROGRESS,
            estimatedMinutes = template.estimatedMinutes,
            createdAt = now,
            completedAt = null,
        )

        return recoveryMissionRepositoryPort.save(mission).toResult()
    }

    private fun com.example.naroo.diagnostic.domain.DiagnosticResult.nextRecoveryConceptAfter(
        completedOrCreatedConcepts: Set<String>,
    ): String? {
        return weakLinks.firstOrNull { it !in completedOrCreatedConcepts }
            ?: primaryRecoveryConcept.takeIf { it !in completedOrCreatedConcepts }
    }
}

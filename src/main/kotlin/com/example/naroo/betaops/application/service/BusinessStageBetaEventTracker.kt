package com.example.naroo.betaops.application.service

import com.example.naroo.auth.port.`in`.LoggedInUserResult
import com.example.naroo.betaops.domain.BetaEventType
import com.example.naroo.diagnostic.port.`in`.CreatedDiagnosticSessionResult
import com.example.naroo.diagnostic.port.`in`.SelectedStartingPointResult
import com.example.naroo.diagnostic.port.`in`.SubmittedDiagnosticResult
import com.example.naroo.recovery.port.`in`.RecoveryMissionResult
import com.example.naroo.recovery.port.`in`.RecoveryMissionSubmissionResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class BusinessStageBetaEventTracker(
    private val appendBetaEventService: AppendBetaEventService,
    private val clock: Clock,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun loginSucceeded(result: LoggedInUserResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.AUTH_LOGIN_SUCCEEDED,
                userId = result.user.id,
                idempotencyKey = "auth-login-succeeded:${result.user.id}:${result.expiresAt}",
                occurredAt = Instant.now(clock),
                payload = mapOf(
                    "role" to result.user.role,
                    "emailVerified" to result.user.emailVerified,
                    "mathStatus" to result.user.mathStatus.name,
                ),
            ),
        )
    }

    fun startingPointSelected(result: SelectedStartingPointResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.DIAGNOSTIC_STARTING_POINT_SELECTED,
                userId = result.userId,
                diagnosticSessionId = null,
                mathArea = result.mathArea,
                idempotencyKey = "diagnostic-starting-point-selected:${result.id}",
                occurredAt = result.updatedAt,
                payload = mapOf(
                    "selectionId" to result.id,
                    "selectionType" to result.selectionType.name,
                    "notePresent" to (result.note != null),
                ),
            ),
        )
    }

    fun diagnosticSessionCreated(result: CreatedDiagnosticSessionResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.DIAGNOSTIC_SESSION_CREATED,
                userId = result.userId,
                diagnosticSessionId = result.id,
                mathArea = result.mathArea,
                questionSnapshotVersion = result.questionSnapshotVersion,
                idempotencyKey = "diagnostic-session-created:${result.id}",
                occurredAt = result.createdAt,
                payload = mapOf(
                    "startingPointSelectionId" to result.startingPointSelectionId,
                    "status" to result.status.name,
                ),
            ),
        )
    }

    fun diagnosticAnswersSubmitted(userId: String, result: SubmittedDiagnosticResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.DIAGNOSTIC_ANSWERS_SUBMITTED,
                userId = userId,
                diagnosticSessionId = result.diagnosticSessionId,
                mathArea = result.mathArea,
                idempotencyKey = "diagnostic-answers-submitted:${result.diagnosticSessionId}",
                occurredAt = Instant.now(clock),
                payload = mapOf(
                    "status" to result.status.name,
                    "totalQuestionCount" to result.totalQuestionCount,
                    "correctCount" to result.correctCount,
                    "wrongCount" to result.wrongCount,
                    "unknownCount" to result.unknownCount,
                    "primaryRecoveryConcept" to result.primaryRecoveryConcept,
                ),
            ),
        )
    }

    fun recoveryMissionCreated(userId: String, result: RecoveryMissionResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.RECOVERY_MISSION_CREATED,
                userId = userId,
                diagnosticSessionId = result.diagnosticSessionId,
                recoveryMissionId = result.id,
                idempotencyKey = "recovery-mission-created:${result.id}",
                occurredAt = result.createdAt,
                payload = mapOf(
                    "conceptTag" to result.conceptTag,
                    "estimatedMinutes" to result.estimatedMinutes,
                    "status" to result.status.name,
                ),
            ),
        )
    }

    fun recoveryMissionSubmitted(userId: String, result: RecoveryMissionSubmissionResult) {
        track(
            AppendBetaEventCommand(
                eventType = BetaEventType.RECOVERY_MISSION_SUBMITTED,
                userId = userId,
                diagnosticSessionId = result.mission.diagnosticSessionId,
                recoveryMissionId = result.recoveryMissionId,
                idempotencyKey = "recovery-mission-submitted:${result.id}",
                occurredAt = result.submittedAt,
                payload = mapOf(
                    "nextAction" to result.nextAction,
                    "feedbackTitle" to result.feedbackTitle,
                    "missionStatus" to result.mission.status.name,
                ),
            ),
        )
    }

    private fun track(command: AppendBetaEventCommand) {
        try {
            appendBetaEventService.append(command)
        } catch (exception: Exception) {
            logger.info(
                "beta_event_business_flow_continues eventType={} userId={} idempotencyKey={} failureKind={}",
                command.eventType,
                command.userId,
                command.idempotencyKey,
                exception.javaClass.simpleName,
            )
        }
    }
}

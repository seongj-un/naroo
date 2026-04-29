package com.example.naroo.recovery.application.service

import com.example.naroo.recovery.application.RecoveryMissionException
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.recovery.domain.RecoveryMissionStatus
import com.example.naroo.recovery.domain.RecoveryMissionSubmission
import com.example.naroo.recovery.port.`in`.RecoveryMissionSubmissionResult
import com.example.naroo.recovery.port.`in`.SubmitRecoveryMissionCommand
import com.example.naroo.recovery.port.`in`.SubmitRecoveryMissionUseCase
import com.example.naroo.recovery.port.`out`.RecoveryMissionRepositoryPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionIdGeneratorPort
import com.example.naroo.recovery.port.`out`.RecoveryMissionSubmissionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SubmitRecoveryMissionService(
    private val recoveryMissionRepositoryPort: RecoveryMissionRepositoryPort,
    private val recoveryMissionSubmissionRepositoryPort: RecoveryMissionSubmissionRepositoryPort,
    private val recoveryMissionSubmissionIdGeneratorPort: RecoveryMissionSubmissionIdGeneratorPort,
    private val clock: Clock,
) : SubmitRecoveryMissionUseCase {
    override fun submit(command: SubmitRecoveryMissionCommand): RecoveryMissionSubmissionResult {
        val userId = UserId(command.userId)
        val missionId = RecoveryMissionId(command.recoveryMissionId)
        val mission = recoveryMissionRepositoryPort.findById(missionId)
            ?.takeIf { it.userId == userId }
            ?: throw RecoveryMissionException.RecoveryMissionNotFound

        if (mission.status == RecoveryMissionStatus.COMPLETED ||
            recoveryMissionSubmissionRepositoryPort.findByRecoveryMissionId(mission.id) != null
        ) {
            throw RecoveryMissionException.RecoveryMissionAlreadyCompleted
        }

        val answerText = command.answerText.trim()
        if (answerText.isBlank() || answerText.length > MAX_ANSWER_TEXT_LENGTH) {
            throw RecoveryMissionException.InvalidRecoveryMissionSubmission
        }

        val now = Instant.now(clock)
        val feedback = RecoveryMissionFeedback.create(answerText, mission.conceptTag)
        val completedMission = mission.copy(
            status = RecoveryMissionStatus.COMPLETED,
            completedAt = now,
        )
        val submission = RecoveryMissionSubmission(
            id = recoveryMissionSubmissionIdGeneratorPort.generate(),
            recoveryMissionId = mission.id,
            userId = userId,
            answerText = answerText,
            feedbackTitle = feedback.title,
            feedbackMessage = feedback.message,
            nextAction = feedback.nextAction,
            submittedAt = now,
        )

        val savedMission = recoveryMissionRepositoryPort.save(completedMission)
        val savedSubmission = recoveryMissionSubmissionRepositoryPort.save(submission)
        return RecoveryMissionSubmissionResult(
            id = savedSubmission.id.value,
            recoveryMissionId = savedSubmission.recoveryMissionId.value,
            feedbackTitle = savedSubmission.feedbackTitle,
            feedbackMessage = savedSubmission.feedbackMessage,
            nextAction = savedSubmission.nextAction,
            submittedAt = savedSubmission.submittedAt,
            mission = savedMission.toResult(),
        )
    }

    private data class RecoveryMissionFeedback(
        val title: String,
        val message: String,
        val nextAction: String,
    ) {
        companion object {
            fun create(answerText: String, conceptTag: String): RecoveryMissionFeedback {
                return if (answerText.length < 20) {
                    RecoveryMissionFeedback(
                        title = "시작점은 잡았어요",
                        message = "짧게라도 멈춘 지점을 적은 게 중요해요. 다음에는 힌트 하나를 골라 식이나 조건을 한 줄 더 붙여봐요.",
                        nextAction = "$conceptTag 개념에서 한 단계만 더 설명해보기",
                    )
                } else {
                    RecoveryMissionFeedback(
                        title = "복구 기록 완료",
                        message = "풀이 과정을 말로 남겼기 때문에 어디서 이해가 이어졌는지 다시 확인할 수 있어요.",
                        nextAction = "다음 약점 개념 미션 이어가기",
                    )
                }
            }
        }
    }

    companion object {
        private const val MAX_ANSWER_TEXT_LENGTH = 1000
    }
}

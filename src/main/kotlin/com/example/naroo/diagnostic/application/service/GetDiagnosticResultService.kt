package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.port.`in`.DiagnosticResultView
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultUseCase
import com.example.naroo.diagnostic.port.`in`.NextMissionPreviewResult
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionRepositoryPort
import com.example.naroo.user.domain.UserId
import org.springframework.stereotype.Service

@Service
class GetDiagnosticResultService(
    private val diagnosticSessionRepositoryPort: DiagnosticSessionRepositoryPort,
    private val diagnosticResultRepositoryPort: DiagnosticResultRepositoryPort,
) : GetDiagnosticResultUseCase {
    override fun get(command: GetDiagnosticResultCommand): DiagnosticResultView {
        val userId = UserId(command.userId)
        val sessionId = DiagnosticSessionId(command.diagnosticSessionId)
        val session = diagnosticSessionRepositoryPort.findById(sessionId)
            ?.takeIf { it.userId == userId }
            ?: throw DiagnosticException.DiagnosticSessionNotFound

        if (session.status != DiagnosticSessionStatus.COMPLETED) {
            throw DiagnosticException.DiagnosticResultNotReady
        }

        val result = diagnosticResultRepositoryPort.findByDiagnosticSessionId(session.id)
            ?: throw DiagnosticException.DiagnosticResultNotReady

        return result.toView(session.status)
    }

    private fun DiagnosticResult.toView(status: DiagnosticSessionStatus): DiagnosticResultView {
        return DiagnosticResultView(
            diagnosticSessionId = diagnosticSessionId.value,
            mathArea = mathArea,
            status = status,
            totalQuestionCount = totalQuestionCount,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unknownCount = unknownCount,
            weakLinks = weakLinks,
            primaryRecoveryConcept = primaryRecoveryConcept,
            summary = summary,
            nextMissionPreview = NextMissionPreviewResult(
                conceptTag = primaryRecoveryConcept,
                title = "${primaryRecoveryConcept} 10분 복구 미션",
                estimatedMinutes = 10,
                tone = "힌트부터 천천히 시작해요.",
            ),
        )
    }
}

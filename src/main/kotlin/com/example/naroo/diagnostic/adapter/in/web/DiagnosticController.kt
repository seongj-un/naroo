package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import com.example.naroo.diagnostic.application.DiagnosticException
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionUseCase
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticResultUseCase
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsUseCase
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswerCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersCommand
import com.example.naroo.diagnostic.port.`in`.SubmitDiagnosticAnswersUseCase
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/diagnostics")
class DiagnosticController(
    private val selectStartingPointUseCase: SelectStartingPointUseCase,
    private val createDiagnosticSessionUseCase: CreateDiagnosticSessionUseCase,
    private val getDiagnosticQuestionsUseCase: GetDiagnosticQuestionsUseCase,
    private val submitDiagnosticAnswersUseCase: SubmitDiagnosticAnswersUseCase,
    private val getDiagnosticResultUseCase: GetDiagnosticResultUseCase,
) {
    @PostMapping
    fun createDiagnosticSession(): ResponseEntity<APiWrappedResponseDto<CreateDiagnosticSessionResponse>> {
        val authentication = verifiedAuthentication()
        val result = createDiagnosticSessionUseCase.create(
            CreateDiagnosticSessionCommand(userId = authentication.userId),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateDiagnosticSessionResponse(
                id = result.id,
                userId = result.userId,
                startingPointSelectionId = result.startingPointSelectionId,
                mathArea = result.mathArea,
                status = result.status,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            ).toWrappedDto(),
        )
    }

    @PostMapping("/starting-point")
    fun selectStartingPoint(
        @RequestBody request: SelectStartingPointRequest,
    ): ResponseEntity<APiWrappedResponseDto<SelectStartingPointResponse>> {
        val authentication = verifiedAuthentication()

        val result = selectStartingPointUseCase.select(
            SelectStartingPointCommand(
                userId = authentication.userId,
                selectionType = request.selectionType,
                mathArea = request.mathArea,
                note = request.note,
            ),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SelectStartingPointResponse(
                id = result.id,
                userId = result.userId,
                selectionType = result.selectionType,
                mathArea = result.mathArea,
                note = result.note,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            ).toWrappedDto(),
        )
    }

    @GetMapping("/{diagnosticSessionId}/questions")
    fun getQuestions(
        @PathVariable diagnosticSessionId: String,
    ): ResponseEntity<APiWrappedResponseDto<DiagnosticQuestionsResponse>> {
        val authentication = verifiedAuthentication()
        val result = getDiagnosticQuestionsUseCase.get(
            GetDiagnosticQuestionsCommand(
                userId = authentication.userId,
                diagnosticSessionId = diagnosticSessionId,
            ),
        )

        return ResponseEntity.ok(
            DiagnosticQuestionsResponse(
                diagnosticSessionId = result.diagnosticSessionId,
                mathArea = result.mathArea,
                status = result.status,
                questions = result.questions.map { question ->
                    DiagnosticQuestionResponse(
                        id = question.id,
                        prompt = question.prompt,
                        choices = question.choices.map { choice ->
                            DiagnosticQuestionChoiceResponse(
                                id = choice.id,
                                text = choice.text,
                            )
                        },
                    )
                },
            ).toWrappedDto(),
        )
    }

    @PostMapping("/{diagnosticSessionId}/answers")
    fun submitAnswers(
        @PathVariable diagnosticSessionId: String,
        @RequestBody request: SubmitDiagnosticAnswersRequest,
    ): ResponseEntity<APiWrappedResponseDto<SubmitDiagnosticAnswersResponse>> {
        val authentication = verifiedAuthentication()
        val result = submitDiagnosticAnswersUseCase.submit(
            SubmitDiagnosticAnswersCommand(
                userId = authentication.userId,
                diagnosticSessionId = diagnosticSessionId,
                answers = request.answers.map { answer ->
                    SubmitDiagnosticAnswerCommand(
                        questionId = answer.questionId,
                        selectedChoiceId = answer.selectedChoiceId,
                    )
                },
            ),
        )

        return ResponseEntity.ok(
            SubmitDiagnosticAnswersResponse(
                diagnosticSessionId = result.diagnosticSessionId,
                mathArea = result.mathArea,
                status = result.status,
                totalQuestionCount = result.totalQuestionCount,
                correctCount = result.correctCount,
                wrongCount = result.wrongCount,
                unknownCount = result.unknownCount,
                weakLinks = result.weakLinks,
                primaryRecoveryConcept = result.primaryRecoveryConcept,
                summary = result.summary,
            ).toWrappedDto(),
        )
    }

    @GetMapping("/{diagnosticSessionId}/result")
    fun getResult(
        @PathVariable diagnosticSessionId: String,
    ): ResponseEntity<APiWrappedResponseDto<DiagnosticResultResponse>> {
        val authentication = verifiedAuthentication()
        val result = getDiagnosticResultUseCase.get(
            GetDiagnosticResultCommand(
                userId = authentication.userId,
                diagnosticSessionId = diagnosticSessionId,
            ),
        )

        return ResponseEntity.ok(
            DiagnosticResultResponse(
                diagnosticSessionId = result.diagnosticSessionId,
                mathArea = result.mathArea,
                status = result.status,
                totalQuestionCount = result.totalQuestionCount,
                correctCount = result.correctCount,
                wrongCount = result.wrongCount,
                unknownCount = result.unknownCount,
                weakLinks = result.weakLinks,
                primaryRecoveryConcept = result.primaryRecoveryConcept,
                summary = result.summary,
                nextMissionPreview = NextMissionPreviewResponse(
                    conceptTag = result.nextMissionPreview.conceptTag,
                    title = result.nextMissionPreview.title,
                    estimatedMinutes = result.nextMissionPreview.estimatedMinutes,
                    tone = result.nextMissionPreview.tone,
                ),
            ).toWrappedDto(),
        )
    }

    private fun verifiedAuthentication(): JwtAuthentication {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        if (!authentication.emailVerified) {
            throw DiagnosticException.EmailVerificationRequired
        }
        return authentication
    }
}

data class CreateDiagnosticSessionResponse(
    val id: String,
    val userId: String,
    val startingPointSelectionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) : SuccessResponseDto

data class SelectStartingPointRequest(
    val selectionType: StartingPointSelectionType,
    val mathArea: MathArea,
    val note: String? = null,
)

data class SelectStartingPointResponse(
    val id: String,
    val userId: String,
    val selectionType: StartingPointSelectionType,
    val mathArea: MathArea,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) : SuccessResponseDto

data class DiagnosticQuestionsResponse(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val questions: List<DiagnosticQuestionResponse>,
) : SuccessResponseDto

data class DiagnosticQuestionResponse(
    val id: String,
    val prompt: String,
    val choices: List<DiagnosticQuestionChoiceResponse>,
)

data class DiagnosticQuestionChoiceResponse(
    val id: String,
    val text: String,
)

data class SubmitDiagnosticAnswersRequest(
    val answers: List<SubmitDiagnosticAnswerRequest>,
)

data class SubmitDiagnosticAnswerRequest(
    val questionId: String,
    val selectedChoiceId: String,
)

data class SubmitDiagnosticAnswersResponse(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
) : SuccessResponseDto

data class DiagnosticResultResponse(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val totalQuestionCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unknownCount: Int,
    val weakLinks: List<String>,
    val primaryRecoveryConcept: String,
    val summary: String,
    val nextMissionPreview: NextMissionPreviewResponse,
) : SuccessResponseDto

data class NextMissionPreviewResponse(
    val conceptTag: String,
    val title: String,
    val estimatedMinutes: Int,
    val tone: String,
)

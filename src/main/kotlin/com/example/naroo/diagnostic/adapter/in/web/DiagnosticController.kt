package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.diagnostic.domain.DiagnosticSessionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionCommand
import com.example.naroo.diagnostic.port.`in`.CreateDiagnosticSessionUseCase
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsCommand
import com.example.naroo.diagnostic.port.`in`.GetDiagnosticQuestionsUseCase
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import jakarta.servlet.http.HttpServletRequest
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
) {
    @PostMapping
    fun createDiagnosticSession(httpRequest: HttpServletRequest): ResponseEntity<CreateDiagnosticSessionResponse> {
        val authentication = verifiedAuthentication(httpRequest)
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
            ),
        )
    }

    @PostMapping("/starting-point")
    fun selectStartingPoint(
        httpRequest: HttpServletRequest,
        @RequestBody request: SelectStartingPointRequest,
    ): ResponseEntity<SelectStartingPointResponse> {
        val authentication = verifiedAuthentication(httpRequest)

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
            ),
        )
    }

    @GetMapping("/{diagnosticSessionId}/questions")
    fun getQuestions(
        httpRequest: HttpServletRequest,
        @PathVariable diagnosticSessionId: String,
    ): ResponseEntity<DiagnosticQuestionsResponse> {
        val authentication = verifiedAuthentication(httpRequest)
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
            ),
        )
    }

    private fun verifiedAuthentication(httpRequest: HttpServletRequest): JwtAuthentication {
        val authentication = httpRequest.getAttribute(JwtAuthentication.REQUEST_ATTRIBUTE) as JwtAuthentication
        if (!authentication.emailVerified) {
            throw EmailVerificationRequiredException()
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
)

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
)

data class DiagnosticQuestionsResponse(
    val diagnosticSessionId: String,
    val mathArea: MathArea,
    val status: DiagnosticSessionStatus,
    val questions: List<DiagnosticQuestionResponse>,
)

data class DiagnosticQuestionResponse(
    val id: String,
    val prompt: String,
    val choices: List<DiagnosticQuestionChoiceResponse>,
)

data class DiagnosticQuestionChoiceResponse(
    val id: String,
    val text: String,
)

class EmailVerificationRequiredException : RuntimeException("email verification is required")

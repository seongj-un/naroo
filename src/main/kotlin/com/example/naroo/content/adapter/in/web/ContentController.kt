package com.example.naroo.content.adapter.`in`.web

import com.example.naroo.content.port.`in`.DiagnosticQuestionChoiceContentResult
import com.example.naroo.content.port.`in`.DiagnosticQuestionContentResult
import com.example.naroo.content.port.`in`.ListDiagnosticQuestionContentsUseCase
import com.example.naroo.content.port.`in`.ListRecoveryMissionTemplateContentsUseCase
import com.example.naroo.content.port.`in`.RecoveryMissionTemplateContentResult
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import com.example.naroo.infrastructure.web.dto.toWrappedDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contents")
class ContentController(
    private val listDiagnosticQuestionContentsUseCase: ListDiagnosticQuestionContentsUseCase,
    private val listRecoveryMissionTemplateContentsUseCase: ListRecoveryMissionTemplateContentsUseCase,
) {
    @GetMapping("/diagnostic-questions")
    fun listDiagnosticQuestions(): APiWrappedResponseDto<DiagnosticQuestionContentsResponse> {
        return DiagnosticQuestionContentsResponse(
            questions = listDiagnosticQuestionContentsUseCase.list().map(DiagnosticQuestionContentResult::toResponse),
        ).toWrappedDto()
    }

    @GetMapping("/recovery-mission-templates")
    fun listRecoveryMissionTemplates(): APiWrappedResponseDto<RecoveryMissionTemplateContentsResponse> {
        return RecoveryMissionTemplateContentsResponse(
            templates = listRecoveryMissionTemplateContentsUseCase.list().map(RecoveryMissionTemplateContentResult::toResponse),
        ).toWrappedDto()
    }
}

data class DiagnosticQuestionContentsResponse(
    val questions: List<DiagnosticQuestionContentResponse>,
) : SuccessResponseDto

data class DiagnosticQuestionContentResponse(
    val id: String,
    val mathArea: MathArea,
    val prompt: String,
    val correctChoiceId: String,
    val conceptTag: String,
    val displayOrder: Int,
    val status: String,
    val choices: List<DiagnosticQuestionChoiceContentResponse>,
)

data class DiagnosticQuestionChoiceContentResponse(
    val id: String,
    val text: String,
)

data class RecoveryMissionTemplateContentsResponse(
    val templates: List<RecoveryMissionTemplateContentResponse>,
) : SuccessResponseDto

data class RecoveryMissionTemplateContentResponse(
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val estimatedMinutes: Int,
    val status: String,
)

private fun DiagnosticQuestionContentResult.toResponse(): DiagnosticQuestionContentResponse {
    return DiagnosticQuestionContentResponse(
        id = id,
        mathArea = mathArea,
        prompt = prompt,
        correctChoiceId = correctChoiceId,
        conceptTag = conceptTag,
        displayOrder = displayOrder,
        status = status,
        choices = choices.map(DiagnosticQuestionChoiceContentResult::toResponse),
    )
}

private fun DiagnosticQuestionChoiceContentResult.toResponse(): DiagnosticQuestionChoiceContentResponse {
    return DiagnosticQuestionChoiceContentResponse(
        id = id,
        text = text,
    )
}

private fun RecoveryMissionTemplateContentResult.toResponse(): RecoveryMissionTemplateContentResponse {
    return RecoveryMissionTemplateContentResponse(
        conceptTag = conceptTag,
        title = title,
        prompt = prompt,
        hints = hints,
        estimatedMinutes = estimatedMinutes,
        status = status,
    )
}

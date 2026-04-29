package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.domain.StartingPointSelectionType
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointCommand
import com.example.naroo.diagnostic.port.`in`.SelectStartingPointUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/diagnostics")
class DiagnosticController(
    private val selectStartingPointUseCase: SelectStartingPointUseCase,
) {
    @PostMapping("/starting-point")
    fun selectStartingPoint(
        httpRequest: HttpServletRequest,
        @RequestBody request: SelectStartingPointRequest,
    ): ResponseEntity<SelectStartingPointResponse> {
        val authentication = httpRequest.getAttribute(JwtAuthentication.REQUEST_ATTRIBUTE) as JwtAuthentication
        if (!authentication.emailVerified) {
            throw EmailVerificationRequiredException()
        }

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
}

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

class EmailVerificationRequiredException : RuntimeException("email verification is required")

package com.example.naroo.diagnostic.adapter.`in`.web

import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`in`.ListMathAreasUseCase
import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.SuccessResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/math-areas")
class MathAreaController(
    private val listMathAreasUseCase: ListMathAreasUseCase,
) {
    @GetMapping
    fun list(): ResponseEntity<APiWrappedResponseDto<List<MathAreaResponse>>> {
        return ResponseEntity.ok(
            APiWrappedResponseDto.success(
                listMathAreasUseCase.list().map {
                    MathAreaResponse(
                        code = it.code,
                        name = it.name,
                        description = it.description,
                        recommendedFor = it.recommendedFor,
                        displayOrder = it.displayOrder,
                    )
                },
            ),
        )
    }
}

data class MathAreaResponse(
    val code: MathArea,
    val name: String,
    val description: String,
    val recommendedFor: String,
    val displayOrder: Int,
) : SuccessResponseDto

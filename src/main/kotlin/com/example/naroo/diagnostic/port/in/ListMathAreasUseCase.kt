package com.example.naroo.diagnostic.port.`in`

import com.example.naroo.diagnostic.domain.MathArea

fun interface ListMathAreasUseCase {
    fun list(): List<MathAreaResult>
}

data class MathAreaResult(
    val code: MathArea,
    val name: String,
    val description: String,
    val recommendedFor: String,
    val displayOrder: Int,
)

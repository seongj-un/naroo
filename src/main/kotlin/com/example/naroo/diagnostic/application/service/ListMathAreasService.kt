package com.example.naroo.diagnostic.application.service

import com.example.naroo.diagnostic.domain.MathAreaCatalog
import com.example.naroo.diagnostic.port.`in`.ListMathAreasUseCase
import com.example.naroo.diagnostic.port.`in`.MathAreaResult
import org.springframework.stereotype.Service

@Service
class ListMathAreasService : ListMathAreasUseCase {
    override fun list(): List<MathAreaResult> {
        return MathAreaCatalog.list().map {
            MathAreaResult(
                code = it.code,
                name = it.name,
                description = it.description,
                recommendedFor = it.recommendedFor,
                displayOrder = it.displayOrder,
            )
        }
    }
}

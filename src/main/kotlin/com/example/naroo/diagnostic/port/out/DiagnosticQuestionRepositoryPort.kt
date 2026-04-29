package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.MathArea

interface DiagnosticQuestionRepositoryPort {
    fun findByMathArea(mathArea: MathArea): List<DiagnosticQuestion>
    fun findAll(): List<DiagnosticQuestion>
}

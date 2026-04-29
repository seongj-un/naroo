package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticAnswer
import com.example.naroo.diagnostic.domain.DiagnosticSessionId

interface DiagnosticAnswerRepositoryPort {
    fun existsByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): Boolean
    fun saveAll(answers: List<DiagnosticAnswer>): List<DiagnosticAnswer>
}

package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSessionId

interface DiagnosticResultRepositoryPort {
    fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult?
    fun save(result: DiagnosticResult): DiagnosticResult
}

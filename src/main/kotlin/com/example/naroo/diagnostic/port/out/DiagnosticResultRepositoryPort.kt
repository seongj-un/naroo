package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.user.domain.UserId

interface DiagnosticResultRepositoryPort {
    fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult?
    fun findLatestByUserId(userId: UserId): DiagnosticResult?
    fun save(result: DiagnosticResult): DiagnosticResult
}

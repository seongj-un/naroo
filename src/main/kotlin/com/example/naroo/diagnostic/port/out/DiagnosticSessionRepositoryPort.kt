package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticSession
import com.example.naroo.diagnostic.domain.DiagnosticSessionId

interface DiagnosticSessionRepositoryPort {
    fun findById(id: DiagnosticSessionId): DiagnosticSession?
    fun save(session: DiagnosticSession): DiagnosticSession
}

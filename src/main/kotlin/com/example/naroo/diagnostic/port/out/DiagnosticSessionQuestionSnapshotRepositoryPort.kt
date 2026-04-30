package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticSessionId

interface DiagnosticSessionQuestionSnapshotRepositoryPort {
    fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): List<DiagnosticQuestion>
    fun saveSnapshot(diagnosticSessionId: DiagnosticSessionId, questions: List<DiagnosticQuestion>): List<DiagnosticQuestion>
}

package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.port.`out`.DiagnosticSessionIdGeneratorPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidDiagnosticSessionIdGeneratorAdapter : DiagnosticSessionIdGeneratorPort {
    override fun generate(): DiagnosticSessionId {
        return DiagnosticSessionId(UUID.randomUUID().toString())
    }
}

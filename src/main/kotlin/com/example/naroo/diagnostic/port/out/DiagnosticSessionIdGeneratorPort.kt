package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticSessionId

fun interface DiagnosticSessionIdGeneratorPort {
    fun generate(): DiagnosticSessionId
}

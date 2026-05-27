package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaFunnelRow

interface BetaFunnelProjectionRepositoryPort {
    fun replaceAll(rows: List<BetaFunnelRow>): List<BetaFunnelRow>
}

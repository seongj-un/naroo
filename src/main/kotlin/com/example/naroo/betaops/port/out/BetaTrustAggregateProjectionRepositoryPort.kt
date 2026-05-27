package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaTrustAggregateRow

interface BetaTrustAggregateProjectionRepositoryPort {
    fun replaceAll(rows: List<BetaTrustAggregateRow>): List<BetaTrustAggregateRow>
}

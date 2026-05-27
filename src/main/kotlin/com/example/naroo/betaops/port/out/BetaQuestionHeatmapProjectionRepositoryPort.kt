package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaQuestionHeatmapRow

interface BetaQuestionHeatmapProjectionRepositoryPort {
    fun replaceAll(rows: List<BetaQuestionHeatmapRow>): List<BetaQuestionHeatmapRow>
}

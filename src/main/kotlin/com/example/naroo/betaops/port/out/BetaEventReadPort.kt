package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaEvent

interface BetaEventReadPort {
    fun findAllOrderByOccurredAtAscReceivedAtAsc(): List<BetaEvent>
}

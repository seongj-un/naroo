package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaEvent

fun interface BetaEventRepositoryPort {
    fun save(event: BetaEvent): BetaEvent
}

class DuplicateBetaEventIdempotencyKeyException(
    val idempotencyKey: String,
    cause: Throwable? = null,
) : RuntimeException("duplicate beta event idempotency key: $idempotencyKey", cause)

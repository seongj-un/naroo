package com.example.naroo.betaops.port.`in`

import com.example.naroo.betaops.domain.BetaTesterTargetMatch

fun interface UpsertBetaTesterProfileUseCase {
    fun upsert(command: UpsertBetaTesterProfileCommand): BetaTesterProfileResult
}

data class UpsertBetaTesterProfileCommand(
    val userId: String,
    val cohortTag: String? = null,
    val targetMatch: BetaTesterTargetMatch? = null,
    val operatorNote: String? = null,
)

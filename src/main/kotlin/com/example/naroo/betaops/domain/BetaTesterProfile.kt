package com.example.naroo.betaops.domain

import java.time.Instant

data class BetaTesterProfile(
    val userId: String,
    val cohortTag: String? = null,
    val targetMatch: BetaTesterTargetMatch? = null,
    val operatorNote: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class BetaTesterTargetMatch {
    TARGET,
    PARTIAL,
    OFF_TARGET,
}

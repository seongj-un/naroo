package com.example.naroo.betaops.port.`out`

import com.example.naroo.betaops.domain.BetaTesterProfile

interface BetaTesterProfileRepositoryPort {
    fun findAll(): List<BetaTesterProfile>

    fun findByUserId(userId: String): BetaTesterProfile?

    fun save(profile: BetaTesterProfile): BetaTesterProfile
}

package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.StartingPointSelection
import com.example.naroo.user.domain.UserId

interface StartingPointSelectionRepositoryPort {
    fun findByUserId(userId: UserId): StartingPointSelection?
    fun save(selection: StartingPointSelection): StartingPointSelection
}

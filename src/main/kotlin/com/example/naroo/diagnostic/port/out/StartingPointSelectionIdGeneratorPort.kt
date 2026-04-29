package com.example.naroo.diagnostic.port.`out`

import com.example.naroo.diagnostic.domain.StartingPointSelectionId

fun interface StartingPointSelectionIdGeneratorPort {
    fun generate(): StartingPointSelectionId
}

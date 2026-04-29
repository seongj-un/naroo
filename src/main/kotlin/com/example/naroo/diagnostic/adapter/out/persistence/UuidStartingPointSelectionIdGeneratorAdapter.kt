package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.StartingPointSelectionId
import com.example.naroo.diagnostic.port.`out`.StartingPointSelectionIdGeneratorPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidStartingPointSelectionIdGeneratorAdapter : StartingPointSelectionIdGeneratorPort {
    override fun generate(): StartingPointSelectionId {
        return StartingPointSelectionId(UUID.randomUUID().toString())
    }
}

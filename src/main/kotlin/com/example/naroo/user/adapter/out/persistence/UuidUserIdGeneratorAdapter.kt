package com.example.naroo.user.adapter.`out`.persistence

import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidUserIdGeneratorAdapter : UserIdGeneratorPort {
    override fun generate(): UserId {
        return UserId(UUID.randomUUID().toString())
    }
}

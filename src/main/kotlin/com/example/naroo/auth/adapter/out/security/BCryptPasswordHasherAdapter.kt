package com.example.naroo.auth.adapter.`out`.security

import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.user.domain.PasswordHash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasherAdapter : PasswordHasherPort {
    private val encoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): PasswordHash {
        return PasswordHash(encoder.encode(rawPassword) ?: error("password hashing failed"))
    }

}

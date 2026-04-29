package com.example.naroo.user.adapter.`out`.security

import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.port.`out`.PasswordHasherPort
import com.example.naroo.user.port.`out`.PasswordVerifierPort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasherAdapter : PasswordHasherPort, PasswordVerifierPort {
    private val encoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): PasswordHash {
        return PasswordHash(encoder.encode(rawPassword) ?: error("password hashing failed"))
    }

    override fun matches(rawPassword: String, passwordHash: PasswordHash): Boolean {
        return encoder.matches(rawPassword, passwordHash.value)
    }
}

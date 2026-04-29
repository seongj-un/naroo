package com.example.naroo.auth.adapter.`out`.security

import com.example.naroo.auth.port.`out`.PasswordVerifierPort
import com.example.naroo.user.domain.PasswordHash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordVerifierAdapter : PasswordVerifierPort {
    private val encoder = BCryptPasswordEncoder()

    override fun matches(rawPassword: String, passwordHash: PasswordHash): Boolean {
        return encoder.matches(rawPassword, passwordHash.value)
    }
}

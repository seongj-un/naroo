package com.example.naroo.auth.port.`out`

import com.example.naroo.user.domain.PasswordHash

fun interface PasswordVerifierPort {
    fun matches(rawPassword: String, passwordHash: PasswordHash): Boolean
}

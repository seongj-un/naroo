package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.PasswordHash

fun interface PasswordVerifierPort {
    fun matches(rawPassword: String, passwordHash: PasswordHash): Boolean
}

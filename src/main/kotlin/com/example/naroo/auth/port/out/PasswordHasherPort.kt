package com.example.naroo.auth.port.`out`

import com.example.naroo.user.domain.PasswordHash

fun interface PasswordHasherPort {
    fun hash(rawPassword: String): PasswordHash
}

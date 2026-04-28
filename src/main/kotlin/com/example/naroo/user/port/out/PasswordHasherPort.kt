package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.PasswordHash

fun interface PasswordHasherPort {
    fun hash(rawPassword: String): PasswordHash
}

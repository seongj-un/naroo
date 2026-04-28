package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.UserId

fun interface UserIdGeneratorPort {
    fun generate(): UserId
}

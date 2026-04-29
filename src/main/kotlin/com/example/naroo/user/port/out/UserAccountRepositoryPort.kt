package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.UserAccount

interface UserAccountRepositoryPort {
    fun existsByLoginId(loginId: LoginId): Boolean
    fun findByLoginId(loginId: LoginId): UserAccount?
    fun save(userAccount: UserAccount): UserAccount
}

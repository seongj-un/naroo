package com.example.naroo.user.port.`out`

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId

interface UserAccountRepositoryPort {
    fun existsByLoginId(loginId: LoginId): Boolean
    fun existsByEmail(email: EmailAddress): Boolean
    fun findByLoginId(loginId: LoginId): UserAccount?
    fun findById(userId: UserId): UserAccount?
    fun save(userAccount: UserAccount): UserAccount
}

class DuplicateUserAccountException(message: String) : RuntimeException(message)

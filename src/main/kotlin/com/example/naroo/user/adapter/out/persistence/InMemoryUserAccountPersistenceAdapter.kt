package com.example.naroo.user.adapter.`out`.persistence

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryUserAccountPersistenceAdapter : UserAccountRepositoryPort {
    private val usersByLoginId = ConcurrentHashMap<String, UserAccount>()

    override fun existsByLoginId(loginId: LoginId): Boolean {
        return usersByLoginId.containsKey(loginId.value)
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return usersByLoginId[loginId.value]
    }

    override fun findById(userId: UserId): UserAccount? {
        return usersByLoginId.values.firstOrNull { it.id == userId }
    }

    override fun save(userAccount: UserAccount): UserAccount {
        usersByLoginId[userAccount.loginId.value] = userAccount
        return userAccount
    }
}

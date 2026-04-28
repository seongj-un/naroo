package com.example.naroo.user.domain

import java.time.Instant

data class UserAccount(
    val id: UserId,
    val loginId: LoginId,
    val passwordHash: PasswordHash,
    val nickname: Nickname,
    val mathStatus: MathStatus,
    val createdAt: Instant,
)

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "userId must not be blank" }
    }
}

@JvmInline
value class LoginId private constructor(val value: String) {
    companion object {
        private val pattern = Regex("^[a-zA-Z0-9._-]{4,30}$")

        fun from(value: String): LoginId {
            val normalized = value.trim()
            require(pattern.matches(normalized)) { "loginId must be 4-30 letters, numbers, dots, underscores, or hyphens" }
            return LoginId(normalized)
        }
    }
}

@JvmInline
value class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "passwordHash must not be blank" }
    }
}

@JvmInline
value class Nickname private constructor(val value: String) {
    companion object {
        fun from(value: String): Nickname {
            val normalized = value.trim()
            require(normalized.length in 2..20) { "nickname must be 2-20 characters" }
            return Nickname(normalized)
        }
    }
}

enum class MathStatus {
    FOLLOWS_CLASS,
    BARELY_FOLLOWS,
    MOSTLY_GAVE_UP,
    UNKNOWN,
}

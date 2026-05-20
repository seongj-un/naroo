package com.example.naroo.user.adapter.`out`.persistence

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.PasswordHash
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.domain.UserRole
import com.example.naroo.user.port.`out`.DuplicateUserAccountException
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaUserAccountPersistenceAdapter(
    private val repository: SpringDataUserAccountJpaRepository,
) : UserAccountRepositoryPort {
    override fun existsByLoginId(loginId: LoginId): Boolean {
        return repository.existsByLoginId(loginId.value)
    }

    override fun existsByEmail(email: EmailAddress): Boolean {
        return repository.existsByEmail(email.value)
    }

    override fun findByLoginId(loginId: LoginId): UserAccount? {
        return repository.findByLoginId(loginId.value)?.toDomain()
    }

    override fun findById(userId: UserId): UserAccount? {
        return repository.findById(userId.value).map(UserAccountJpaEntity::toDomain).orElse(null)
    }

    override fun save(userAccount: UserAccount): UserAccount {
        try {
            return repository.saveAndFlush(UserAccountJpaEntity.from(userAccount)).toDomain()
        } catch (_: DataIntegrityViolationException) {
            throw DuplicateUserAccountException("loginId or email already exists")
        }
    }

    override fun deleteById(userId: UserId) {
        repository.deleteById(userId.value)
    }
}

interface SpringDataUserAccountJpaRepository : JpaRepository<UserAccountJpaEntity, String> {
    fun existsByLoginId(loginId: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun findByLoginId(loginId: String): UserAccountJpaEntity?
}

@Entity
@Table(name = "user_accounts")
class UserAccountJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = "",

    @Column(name = "login_id", nullable = false, unique = true, length = 30)
    var loginId: String = "",

    @Column(name = "email", nullable = false, unique = true, length = 254)
    var email: String = "",

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",

    @Column(name = "nickname", nullable = false, length = 20)
    var nickname: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "math_status", nullable = false, length = 32)
    var mathStatus: MathStatus = MathStatus.UNKNOWN,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    var role: UserRole = UserRole.STUDENT,

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): UserAccount {
        return UserAccount(
            id = UserId(id),
            loginId = LoginId.from(loginId),
            email = EmailAddress.from(email),
            emailVerified = emailVerified,
            passwordHash = PasswordHash(passwordHash),
            nickname = Nickname.from(nickname),
            mathStatus = mathStatus,
            role = role,
            createdAt = createdAt,
        )
    }

    companion object {
        fun from(userAccount: UserAccount): UserAccountJpaEntity {
            return UserAccountJpaEntity(
                id = userAccount.id.value,
                loginId = userAccount.loginId.value,
                email = userAccount.email.value,
                emailVerified = userAccount.emailVerified,
                passwordHash = userAccount.passwordHash.value,
                nickname = userAccount.nickname.value,
                mathStatus = userAccount.mathStatus,
                role = userAccount.role,
                createdAt = userAccount.createdAt,
            )
        }
    }
}

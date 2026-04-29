package com.example.naroo.auth.application.service

import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`in`.SignUpUserUseCase
import com.example.naroo.auth.port.`in`.SignedUpUserResult
import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.port.`out`.DuplicateUserAccountException
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SignUpUserService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val passwordHasherPort: PasswordHasherPort,
    private val userIdGeneratorPort: UserIdGeneratorPort,
    private val clock: Clock = Clock.systemUTC(),
) : SignUpUserUseCase {
    override fun signUp(command: SignUpUserCommand): SignedUpUserResult {
        val loginId = LoginId.from(command.loginId)
        val email = EmailAddress.from(command.email)
        val nickname = Nickname.from(command.nickname)
        require(command.password.length >= 8) { "password must be at least 8 characters" }

        if (userAccountRepositoryPort.existsByLoginId(loginId)) {
            throw DuplicateLoginIdException(loginId.value)
        }
        if (userAccountRepositoryPort.existsByEmail(email)) {
            throw DuplicateEmailException(email.value)
        }

        val userAccount = UserAccount(
            id = userIdGeneratorPort.generate(),
            loginId = loginId,
            email = email,
            emailVerified = false,
            passwordHash = passwordHasherPort.hash(command.password),
            nickname = nickname,
            mathStatus = command.mathStatus,
            createdAt = Instant.now(clock),
        )

        val saved = try {
            userAccountRepositoryPort.save(userAccount)
        } catch (_: DuplicateUserAccountException) {
            if (userAccountRepositoryPort.existsByEmail(email)) {
                throw DuplicateEmailException(email.value)
            }
            throw DuplicateLoginIdException(loginId.value)
        }
        return SignedUpUserResult(
            id = saved.id,
            loginId = saved.loginId.value,
            email = saved.email.value,
            emailVerified = saved.emailVerified,
            nickname = saved.nickname.value,
            mathStatus = saved.mathStatus,
            createdAt = saved.createdAt,
        )
    }
}

class DuplicateLoginIdException(loginId: String) : RuntimeException("loginId already exists: $loginId")
class DuplicateEmailException(email: String) : RuntimeException("email already exists: $email")

package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.SignUpUserCommand
import com.example.naroo.auth.port.`in`.SignUpUserUseCase
import com.example.naroo.auth.port.`in`.SignedUpUserResult
import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.TokenStorePort
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
    private val emailVerificationTokenPort: EmailVerificationTokenPort,
    private val tokenStorePort: TokenStorePort,
    private val emailSenderPort: EmailSenderPort,
    private val clock: Clock = Clock.systemUTC(),
) : SignUpUserUseCase {
    override fun signUp(command: SignUpUserCommand): SignedUpUserResult {
        val loginId = LoginId.from(command.loginId)
        val email = EmailAddress.from(command.email)
        val nickname = Nickname.from(command.nickname)
        require(command.password.length >= 8) { "password must be at least 8 characters" }

        if (userAccountRepositoryPort.existsByLoginId(loginId)) {
            throw AuthException.LoginIdAlreadyExists
        }
        if (userAccountRepositoryPort.existsByEmail(email)) {
            throw AuthException.EmailAlreadyExists
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
                throw AuthException.EmailAlreadyExists
            }
            throw AuthException.LoginIdAlreadyExists
        }

        val verificationToken = emailVerificationTokenPort.issue(saved.id.value, saved.email.value)
        tokenStorePort.saveEmailVerificationToken(
            StoredEmailVerificationToken(
                tokenId = verificationToken.id,
                userId = saved.id.value,
                email = saved.email.value,
                tokenHash = verificationToken.tokenHash,
                expiresAt = verificationToken.expiresAt,
            ),
        )
        emailSenderPort.sendEmailVerification(
            EmailVerificationMessage(
                userId = saved.id.value,
                email = saved.email.value,
                token = verificationToken.value,
            ),
        )

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

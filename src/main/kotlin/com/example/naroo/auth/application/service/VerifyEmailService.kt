package com.example.naroo.auth.application.service

import com.example.naroo.auth.port.`in`.VerifiedEmailResult
import com.example.naroo.auth.port.`in`.VerifyEmailCommand
import com.example.naroo.auth.port.`in`.VerifyEmailUseCase
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class VerifyEmailService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val emailVerificationTokenPort: EmailVerificationTokenPort,
    private val tokenStorePort: TokenStorePort,
) : VerifyEmailUseCase {
    override fun verify(command: VerifyEmailCommand): VerifiedEmailResult {
        val tokenId = command.token.substringBefore('.', missingDelimiterValue = "")
        if (tokenId.isBlank()) {
            throw InvalidEmailVerificationTokenException()
        }

        val storedToken = tokenStorePort.consumeEmailVerificationToken(tokenId)
            ?: throw InvalidEmailVerificationTokenException()

        if (storedToken.tokenHash != emailVerificationTokenPort.hash(command.token)) {
            throw InvalidEmailVerificationTokenException()
        }

        val userAccount = userAccountRepositoryPort.findById(UserId(storedToken.userId))
            ?: throw InvalidEmailVerificationTokenException()
        val email = EmailAddress.from(storedToken.email)
        if (userAccount.email != email) {
            throw InvalidEmailVerificationTokenException()
        }

        val verifiedUser = if (userAccount.emailVerified) {
            userAccount
        } else {
            userAccountRepositoryPort.save(userAccount.copy(emailVerified = true))
        }

        return VerifiedEmailResult(
            userId = verifiedUser.id.value,
            email = verifiedUser.email.value,
            emailVerified = verifiedUser.emailVerified,
        )
    }
}

class InvalidEmailVerificationTokenException : RuntimeException("invalid email verification token")

package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.ResendEmailVerificationCommand
import com.example.naroo.auth.port.`in`.ResendEmailVerificationUseCase
import com.example.naroo.auth.port.`in`.ResentEmailVerificationResult
import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import com.example.naroo.auth.port.`out`.EmailVerificationTokenPort
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class ResendEmailVerificationService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val emailVerificationTokenPort: EmailVerificationTokenPort,
    private val tokenStorePort: TokenStorePort,
    private val emailSenderPort: EmailSenderPort,
    @Value("\${naroo.auth.email.resend-cooldown-seconds:60}")
    private val resendCooldownSeconds: Long = 60,
    private val clock: Clock = Clock.systemUTC(),
) : ResendEmailVerificationUseCase {
    override fun resend(command: ResendEmailVerificationCommand): ResentEmailVerificationResult {
        val userAccount = userAccountRepositoryPort.findById(UserId(command.userId))
            ?: throw AuthException.Unauthorized

        if (userAccount.emailVerified) {
            throw AuthException.EmailAlreadyVerified
        }

        val now = Instant.now(clock)
        val nextAllowedAt = tokenStorePort.findEmailVerificationResendAvailableAt(userAccount.id.value)
        if (nextAllowedAt != null && nextAllowedAt.isAfter(now)) {
            throw AuthException.EmailVerificationResendTooSoon(
                retryAfterSeconds = Duration.between(now, nextAllowedAt).seconds.coerceAtLeast(1),
            )
        }

        val verificationToken = emailVerificationTokenPort.issue(userAccount.id.value, userAccount.email.value)
        tokenStorePort.saveEmailVerificationToken(
            StoredEmailVerificationToken(
                tokenId = verificationToken.id,
                userId = userAccount.id.value,
                email = userAccount.email.value,
                tokenHash = verificationToken.tokenHash,
                expiresAt = verificationToken.expiresAt,
            ),
        )

        try {
            emailSenderPort.sendEmailVerification(
                EmailVerificationMessage(
                    userId = userAccount.id.value,
                    email = userAccount.email.value,
                    token = verificationToken.value,
                ),
            )
        } catch (ex: RuntimeException) {
            tokenStorePort.deleteEmailVerificationToken(verificationToken.id)
            throw ex
        }

        val nextRetryAt = now.plusSeconds(resendCooldownSeconds.coerceAtLeast(1))
        tokenStorePort.saveEmailVerificationResendCooldown(
            userId = userAccount.id.value,
            availableAt = nextRetryAt,
        )

        return ResentEmailVerificationResult(
            userId = userAccount.id.value,
            email = userAccount.email.value,
            emailVerified = false,
            nextRetryAt = nextRetryAt,
        )
    }
}

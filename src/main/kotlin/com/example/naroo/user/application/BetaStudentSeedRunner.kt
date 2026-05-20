package com.example.naroo.user.application

import com.example.naroo.auth.port.`out`.PasswordHasherPort
import com.example.naroo.user.domain.EmailAddress
import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.domain.MathStatus
import com.example.naroo.user.domain.Nickname
import com.example.naroo.user.domain.UserAccount
import com.example.naroo.user.domain.UserRole
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import com.example.naroo.user.port.`out`.UserIdGeneratorPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
@Profile("!prod")
class BetaStudentSeedRunner(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val passwordHasherPort: PasswordHasherPort,
    private val userIdGeneratorPort: UserIdGeneratorPort,
    private val clock: Clock,
    @Value("\${naroo.seed.student.enabled:false}") private val enabled: Boolean,
    @Value("\${naroo.seed.student.login-id:student01}") private val seedLoginId: String,
    @Value("\${naroo.seed.student.email:student01@example.com}") private val seedEmail: String,
    @Value("\${naroo.seed.student.password:password123}") private val seedPassword: String,
    @Value("\${naroo.seed.student.nickname:나루}") private val seedNickname: String,
    @Value("\${naroo.seed.student.math-status:UNKNOWN}") private val seedMathStatus: MathStatus,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!enabled) {
            return
        }

        val loginId = LoginId.from(seedLoginId)
        if (userAccountRepositoryPort.existsByLoginId(loginId)) {
            logger.info("beta student seed skipped because loginId={} already exists", loginId.value)
            return
        }

        val email = EmailAddress.from(seedEmail)
        if (userAccountRepositoryPort.existsByEmail(email)) {
            logger.info("beta student seed skipped because email={} already exists", email.value)
            return
        }

        val saved = userAccountRepositoryPort.save(
            UserAccount(
                id = userIdGeneratorPort.generate(),
                loginId = loginId,
                email = email,
                emailVerified = true,
                passwordHash = passwordHasherPort.hash(seedPassword),
                nickname = Nickname.from(seedNickname),
                mathStatus = seedMathStatus,
                role = UserRole.STUDENT,
                createdAt = Instant.now(clock),
            ),
        )

        logger.info("beta student seed created loginId={} email={}", saved.loginId.value, saved.email.value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BetaStudentSeedRunner::class.java)
    }
}

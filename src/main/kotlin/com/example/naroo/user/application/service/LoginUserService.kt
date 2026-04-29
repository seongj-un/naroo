package com.example.naroo.user.application.service

import com.example.naroo.user.domain.LoginId
import com.example.naroo.user.port.`in`.LoggedInUser
import com.example.naroo.user.port.`in`.LoggedInUserResult
import com.example.naroo.user.port.`in`.LoginUserCommand
import com.example.naroo.user.port.`in`.LoginUserUseCase
import com.example.naroo.user.port.`out`.JwtTokenIssuerPort
import com.example.naroo.user.port.`out`.PasswordVerifierPort
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class LoginUserService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val passwordVerifierPort: PasswordVerifierPort,
    private val jwtTokenIssuerPort: JwtTokenIssuerPort,
) : LoginUserUseCase {
    override fun login(command: LoginUserCommand): LoggedInUserResult {
        val loginId = LoginId.from(command.loginId)
        val userAccount = userAccountRepositoryPort.findByLoginId(loginId)
            ?: throw InvalidLoginCredentialsException()

        if (!passwordVerifierPort.matches(command.password, userAccount.passwordHash)) {
            throw InvalidLoginCredentialsException()
        }

        val token = jwtTokenIssuerPort.issue(userAccount)
        return LoggedInUserResult(
            accessToken = token.value,
            tokenType = "Bearer",
            expiresAt = token.expiresAt,
            user = LoggedInUser(
                id = userAccount.id.value,
                loginId = userAccount.loginId.value,
                nickname = userAccount.nickname.value,
                mathStatus = userAccount.mathStatus,
            ),
        )
    }
}

class InvalidLoginCredentialsException : RuntimeException("invalid login credentials")

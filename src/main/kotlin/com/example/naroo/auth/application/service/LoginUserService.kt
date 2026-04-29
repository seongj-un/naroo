package com.example.naroo.auth.application.service

import com.example.naroo.user.domain.LoginId
import com.example.naroo.auth.port.`in`.LoggedInUser
import com.example.naroo.auth.port.`in`.LoggedInUserResult
import com.example.naroo.auth.port.`in`.LoginUserCommand
import com.example.naroo.auth.port.`in`.LoginUserUseCase
import com.example.naroo.auth.port.`out`.JwtTokenIssuerPort
import com.example.naroo.auth.port.`out`.PasswordVerifierPort
import com.example.naroo.auth.port.`out`.RefreshTokenPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class LoginUserService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val passwordVerifierPort: PasswordVerifierPort,
    private val jwtTokenIssuerPort: JwtTokenIssuerPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val tokenStorePort: TokenStorePort,
) : LoginUserUseCase {
    override fun login(command: LoginUserCommand): LoggedInUserResult {
        val loginId = LoginId.from(command.loginId)
        val userAccount = userAccountRepositoryPort.findByLoginId(loginId)
            ?: throw InvalidLoginCredentialsException()

        if (!passwordVerifierPort.matches(command.password, userAccount.passwordHash)) {
            throw InvalidLoginCredentialsException()
        }

        val token = jwtTokenIssuerPort.issue(userAccount)
        val refreshToken = refreshTokenPort.issue(userAccount.id.value)
        tokenStorePort.saveAccessToken(
            StoredAccessToken(
                tokenId = token.id,
                userId = userAccount.id.value,
                expiresAt = token.expiresAt,
            ),
        )
        tokenStorePort.saveRefreshToken(
            StoredRefreshToken(
                tokenId = refreshToken.id,
                userId = userAccount.id.value,
                tokenHash = refreshToken.tokenHash,
                expiresAt = refreshToken.expiresAt,
            ),
        )
        return LoggedInUserResult(
            accessToken = token.value,
            tokenType = "Bearer",
            expiresAt = token.expiresAt,
            refreshToken = refreshToken.value,
            refreshTokenExpiresAt = refreshToken.expiresAt,
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

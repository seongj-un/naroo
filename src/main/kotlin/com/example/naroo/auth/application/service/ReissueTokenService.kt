package com.example.naroo.auth.application.service

import com.example.naroo.auth.application.AuthException
import com.example.naroo.auth.port.`in`.ReissueTokenCommand
import com.example.naroo.auth.port.`in`.ReissueTokenUseCase
import com.example.naroo.auth.port.`in`.ReissuedTokenResult
import com.example.naroo.auth.port.`out`.JwtTokenIssuerPort
import com.example.naroo.auth.port.`out`.RefreshTokenPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.user.domain.UserId
import com.example.naroo.user.port.`out`.UserAccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class ReissueTokenService(
    private val userAccountRepositoryPort: UserAccountRepositoryPort,
    private val jwtTokenIssuerPort: JwtTokenIssuerPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val tokenStorePort: TokenStorePort,
) : ReissueTokenUseCase {
    override fun reissue(command: ReissueTokenCommand): ReissuedTokenResult {
        val refreshTokenId = command.refreshToken.substringBefore('.', missingDelimiterValue = "")
        if (refreshTokenId.isBlank()) {
            throw AuthException.InvalidRefreshToken
        }

        val storedRefreshToken = tokenStorePort.consumeRefreshToken(refreshTokenId)
            ?: throw AuthException.InvalidRefreshToken

        if (storedRefreshToken.tokenHash != refreshTokenPort.hash(command.refreshToken)) {
            throw AuthException.InvalidRefreshToken
        }

        val userAccount = userAccountRepositoryPort.findById(UserId(storedRefreshToken.userId))
            ?: throw AuthException.InvalidRefreshToken

        val accessToken = jwtTokenIssuerPort.issue(userAccount)
        val refreshToken = refreshTokenPort.issue(userAccount.id.value)
        tokenStorePort.saveAccessToken(
            StoredAccessToken(
                tokenId = accessToken.id,
                userId = userAccount.id.value,
                expiresAt = accessToken.expiresAt,
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

        return ReissuedTokenResult(
            accessToken = accessToken.value,
            tokenType = "Bearer",
            expiresAt = accessToken.expiresAt,
            refreshToken = refreshToken.value,
            refreshTokenExpiresAt = refreshToken.expiresAt,
        )
    }
}

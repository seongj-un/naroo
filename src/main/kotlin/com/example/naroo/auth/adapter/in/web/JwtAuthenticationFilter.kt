package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`out`.JwtTokenVerifierPort
import com.example.naroo.auth.port.`out`.TokenStorePort
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter(
    private val jwtTokenVerifierPort: JwtTokenVerifierPort,
    private val tokenStorePort: TokenStorePort,
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        try {
            val rawToken = bearerToken(httpRequest)
            if (rawToken != null) {
                val verifiedToken = jwtTokenVerifierPort.verify(rawToken)
                if (verifiedToken != null) {
                    val storedUserId = tokenStorePort.findUserIdByAccessTokenId(verifiedToken.tokenId)
                    if (storedUserId == verifiedToken.userId) {
                        val authentication = JwtAuthentication(
                            tokenId = verifiedToken.tokenId,
                            userId = verifiedToken.userId,
                            loginId = verifiedToken.loginId,
                            emailVerified = verifiedToken.emailVerified,
                            nickname = verifiedToken.nickname,
                            role = verifiedToken.role,
                        )
                        SecurityContextHolder.getContext().authentication =
                            UsernamePasswordAuthenticationToken(authentication, null, emptyList())
                    }
                }
            }
        } finally {
            chain.doFilter(request, response)
            SecurityContextHolder.clearContext()
        }
    }

    private fun bearerToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader("Authorization") ?: return null
        if (!authorization.startsWith("Bearer ")) {
            return null
        }
        return authorization.removePrefix("Bearer ").trim().takeIf { it.isNotBlank() }
    }
}

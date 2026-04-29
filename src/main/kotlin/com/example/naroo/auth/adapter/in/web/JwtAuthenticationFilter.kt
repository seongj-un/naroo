package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`out`.JwtTokenVerifierPort
import com.example.naroo.auth.port.`out`.TokenStorePort
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter(
    private val jwtTokenVerifierPort: JwtTokenVerifierPort,
    private val tokenStorePort: TokenStorePort,
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        if (!requiresAuthentication(httpRequest)) {
            chain.doFilter(request, response)
            return
        }

        val rawToken = bearerToken(httpRequest)
        if (rawToken == null) {
            reject(httpResponse)
            return
        }

        val verifiedToken = jwtTokenVerifierPort.verify(rawToken)
        if (verifiedToken == null) {
            reject(httpResponse)
            return
        }

        val storedUserId = tokenStorePort.findUserIdByAccessTokenId(verifiedToken.tokenId)
        if (storedUserId != verifiedToken.userId) {
            reject(httpResponse)
            return
        }

        httpRequest.setAttribute(
            JwtAuthentication.REQUEST_ATTRIBUTE,
            JwtAuthentication(
                tokenId = verifiedToken.tokenId,
                userId = verifiedToken.userId,
                loginId = verifiedToken.loginId,
                nickname = verifiedToken.nickname,
            ),
        )
        chain.doFilter(request, response)
    }

    private fun requiresAuthentication(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/api/auth/me")
    }

    private fun bearerToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader("Authorization") ?: return null
        if (!authorization.startsWith("Bearer ")) {
            return null
        }
        return authorization.removePrefix("Bearer ").trim().takeIf { it.isNotBlank() }
    }

    private fun reject(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"message":"unauthorized"}""")
    }
}

package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`out`.JwtTokenVerifierPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredEmailVerificationToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.auth.port.`out`.VerifiedJwtToken
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest {
    @Test
    fun `allows protected request with valid bearer token stored in token store`() {
        val filter = JwtAuthenticationFilter(
            jwtTokenVerifierPort = JwtTokenVerifierPort {
                VerifiedJwtToken(
                    tokenId = "token-1",
                    userId = "user-1",
                    loginId = "student01",
                    emailVerified = false,
                    nickname = "나루",
                    role = "STUDENT",
                )
            },
            tokenStorePort = FakeTokenStore("token-1" to "user-1"),
        )
        val request = MockHttpServletRequest("GET", "/api/auth/me")
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain()
        request.addHeader("Authorization", "Bearer valid-token")

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
        assertEquals(1, chain.calledCount)
        assertEquals("user-1", chain.authentication?.userId)
    }

    @Test
    fun `continues protected request without bearer token and leaves authentication empty`() {
        val filter = JwtAuthenticationFilter(
            jwtTokenVerifierPort = JwtTokenVerifierPort { error("token should not be verified") },
            tokenStorePort = FakeTokenStore(),
        )
        val request = MockHttpServletRequest("GET", "/api/auth/me")
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
        assertEquals(1, chain.calledCount)
        assertNull(chain.authentication)
    }

    @Test
    fun `continues diagnostic endpoints without authentication`() {
        val filter = JwtAuthenticationFilter(
            jwtTokenVerifierPort = JwtTokenVerifierPort { error("token should not be verified") },
            tokenStorePort = FakeTokenStore(),
        )
        val request = MockHttpServletRequest("POST", "/api/diagnostics/starting-point")
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
        assertEquals(1, chain.calledCount)
        assertNull(chain.authentication)
    }
}

private class FakeTokenStore(
    private val token: Pair<String, String>? = null,
) : TokenStorePort {
    override fun saveAccessToken(token: StoredAccessToken) {
        error("token should not be stored")
    }

    override fun findUserIdByAccessTokenId(tokenId: String): String? {
        return token?.takeIf { it.first == tokenId }?.second
    }

    override fun saveRefreshToken(token: StoredRefreshToken) {
        error("refresh token should not be stored")
    }

    override fun consumeRefreshToken(tokenId: String): StoredRefreshToken? {
        error("refresh token should not be consumed")
    }

    override fun saveEmailVerificationToken(token: StoredEmailVerificationToken) {
        error("email verification token should not be stored")
    }

    override fun consumeEmailVerificationToken(tokenId: String): StoredEmailVerificationToken? {
        error("email verification token should not be consumed")
    }
}

private class CapturingFilterChain : FilterChain {
    var calledCount = 0
    var authentication: JwtAuthentication? = null

    override fun doFilter(request: ServletRequest, response: ServletResponse) {
        calledCount += 1
        authentication = SecurityContextHolder.getContext().authentication?.principal as? JwtAuthentication
    }
}

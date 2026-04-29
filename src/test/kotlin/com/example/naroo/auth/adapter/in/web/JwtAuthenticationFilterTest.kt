package com.example.naroo.auth.adapter.`in`.web

import com.example.naroo.auth.port.`out`.JwtTokenVerifierPort
import com.example.naroo.auth.port.`out`.StoredAccessToken
import com.example.naroo.auth.port.`out`.StoredRefreshToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import com.example.naroo.auth.port.`out`.VerifiedJwtToken
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class JwtAuthenticationFilterTest {
    @Test
    fun `allows protected request with valid bearer token stored in token store`() {
        val filter = JwtAuthenticationFilter(
            jwtTokenVerifierPort = JwtTokenVerifierPort {
                VerifiedJwtToken(
                    tokenId = "token-1",
                    userId = "user-1",
                    loginId = "student01",
                    nickname = "나루",
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
        assertNotNull(request.getAttribute(JwtAuthentication.REQUEST_ATTRIBUTE))
    }

    @Test
    fun `rejects protected request without bearer token`() {
        val filter = JwtAuthenticationFilter(
            jwtTokenVerifierPort = JwtTokenVerifierPort { error("token should not be verified") },
            tokenStorePort = FakeTokenStore(),
        )
        val request = MockHttpServletRequest("GET", "/api/auth/me")
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain()

        filter.doFilter(request, response, chain)

        assertEquals(401, response.status)
        assertEquals(0, chain.calledCount)
        assertNull(request.getAttribute(JwtAuthentication.REQUEST_ATTRIBUTE))
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
}

private class CapturingFilterChain : FilterChain {
    var calledCount = 0

    override fun doFilter(request: ServletRequest, response: ServletResponse) {
        calledCount += 1
    }
}

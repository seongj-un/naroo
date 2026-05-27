package com.example.naroo.betaops.adapter.`in`.web

import com.example.naroo.auth.adapter.`in`.web.JwtAuthentication
import com.example.naroo.auth.application.AuthException
import org.springframework.stereotype.Component

@Component
class BetaOpsAdminAuthorizer {
    fun verify() {
        val authentication = JwtAuthentication.current() ?: throw AuthException.Unauthorized
        if (authentication.role != ADMIN_ROLE) {
            throw AuthException.Unauthorized
        }
    }

    companion object {
        private const val ADMIN_ROLE = "ADMIN"
    }
}

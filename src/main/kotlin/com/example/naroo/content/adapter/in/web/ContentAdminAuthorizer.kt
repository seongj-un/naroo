package com.example.naroo.content.adapter.`in`.web

import com.example.naroo.auth.application.AuthException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ContentAdminAuthorizer(
    @Value("\${naroo.content.admin-token:}") private val adminToken: String,
) {
    fun verify(token: String?) {
        if (adminToken.isBlank() || token.isNullOrBlank() || token != adminToken) {
            throw AuthException.Unauthorized
        }
    }
}

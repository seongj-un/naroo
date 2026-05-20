package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class ResendEmailSenderAdapterTest {
    @Test
    fun `sends verification email through resend api`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val adapter = ResendEmailSenderAdapter(
            restClientBuilder = builder,
            fromAddress = "no-reply@naroo.app",
            fromName = "Naroo",
            verificationUrlTemplate = "https://narooflutter.vercel.app/verify-email?token={token}",
            apiKey = "re_test_key",
            apiBaseUrl = "https://api.resend.com",
        )

        server.expect(requestTo("https://api.resend.com/emails"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer re_test_key"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                """
                {
                  "from": "Naroo <no-reply@naroo.app>",
                  "to": ["student@example.com"],
                  "subject": "[Naroo] 이메일 인증을 완료해 주세요"
                }
                """.trimIndent(),
                false,
            ))
            .andRespond(withSuccess("""{"id":"email_123"}""", MediaType.APPLICATION_JSON))

        adapter.sendEmailVerification(
            EmailVerificationMessage(
                userId = "user-1",
                email = "student@example.com",
                token = "email-token-1.secret",
            ),
        )

        server.verify()
    }
}

package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(prefix = "naroo.auth.email", name = ["mode"], havingValue = "resend")
class ResendEmailSenderAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${naroo.auth.email.from-address}") private val fromAddress: String,
    @Value("\${naroo.auth.email.from-name:Naroo}") private val fromName: String,
    @Value("\${naroo.auth.email.verification-url-template}") private val verificationUrlTemplate: String,
    @Value("\${naroo.auth.email.resend.api-key}") private val apiKey: String,
    @Value("\${naroo.auth.email.resend.base-url:https://api.resend.com}") apiBaseUrl: String,
) : EmailSenderPort {
    private val restClient = restClientBuilder
        .baseUrl(apiBaseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    override fun sendEmailVerification(message: EmailVerificationMessage) {
        val verificationUrl = verificationUrlTemplate.replace(
            "{token}",
            UriUtils.encode(message.token, StandardCharsets.UTF_8),
        )
        val payload = ResendSendEmailRequest(
            from = "$fromName <$fromAddress>",
            to = listOf(message.email),
            subject = "[Naroo] 이메일 인증을 완료해 주세요",
            html = buildHtmlBody(verificationUrl, message.token),
            text = buildTextBody(verificationUrl, message.token),
        )

        try {
            val response = restClient.post()
                .uri("/emails")
                .body(payload)
                .retrieve()
                .body(ResendSendEmailResponse::class.java)

            require(!response?.id.isNullOrBlank()) { "resend email response missing id" }
        } catch (ex: RestClientResponseException) {
            throw IllegalStateException(
                "resend email send failed with status ${ex.statusCode.value()}: ${ex.responseBodyAsString}",
                ex,
            )
        }
    }

    private fun buildTextBody(verificationUrl: String, token: String): String {
        return """
            안녕하세요, ${fromName}입니다.
            
            아래 링크를 열어 이메일 인증을 완료해 주세요.
            $verificationUrl
            
            링크가 열리지 않으면 인증 토큰을 직접 입력해 주세요.
            $token
        """.trimIndent()
    }

    private fun buildHtmlBody(verificationUrl: String, token: String): String {
        return """
            <p>안녕하세요, ${escapeHtml(fromName)}입니다.</p>
            <p>아래 링크를 열어 이메일 인증을 완료해 주세요.</p>
            <p><a href="$verificationUrl">$verificationUrl</a></p>
            <p>링크가 열리지 않으면 인증 토큰을 직접 입력해 주세요.</p>
            <p><code>${escapeHtml(token)}</code></p>
        """.trimIndent()
    }

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

private data class ResendSendEmailRequest(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String,
    val text: String,
)

private data class ResendSendEmailResponse(
    val id: String?,
)

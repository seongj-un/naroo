package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(prefix = "naroo.auth.email", name = ["mode"], havingValue = "smtp")
class SmtpEmailSenderAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${naroo.auth.email.from-address}") private val fromAddress: String,
    @Value("\${naroo.auth.email.from-name:Naroo}") private val fromName: String,
    @Value("\${naroo.auth.email.verification-url-template}") private val verificationUrlTemplate: String,
) : EmailSenderPort {
    override fun sendEmailVerification(message: EmailVerificationMessage) {
        val verificationUrl = verificationUrlTemplate.replace(
            "{token}",
            UriUtils.encode(message.token, StandardCharsets.UTF_8),
        )

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name())
        helper.setTo(message.email)
        helper.setSubject("[Naroo] 이메일 인증을 완료해 주세요")
        helper.setFrom(fromAddress, fromName)
        helper.setText(
            """
            안녕하세요, ${fromName}입니다.
            
            아래 링크를 열어 이메일 인증을 완료해 주세요.
            $verificationUrl
            
            링크가 열리지 않으면 인증 토큰을 직접 입력해 주세요.
            ${message.token}
            """.trimIndent(),
            false,
        )

        mailSender.send(mimeMessage)
    }
}

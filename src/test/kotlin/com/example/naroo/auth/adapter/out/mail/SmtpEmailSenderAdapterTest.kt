package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import java.util.Properties

class SmtpEmailSenderAdapterTest {
    @Test
    fun `sends verification email with frontend verification link`() {
        val mailSender = CapturingJavaMailSender()
        val adapter = SmtpEmailSenderAdapter(
            mailSender = mailSender,
            fromAddress = "no-reply@naroo.app",
            fromName = "Naroo",
            verificationUrlTemplate = "https://naroo.app/verify-email?token={token}",
        )

        adapter.sendEmailVerification(
            EmailVerificationMessage(
                userId = "user-1",
                email = "student@example.com",
                token = "email-token-1.secret",
            ),
        )

        val sent = mailSender.sent.single()
        assertEquals("student@example.com", (sent.getRecipients(Message.RecipientType.TO).single() as InternetAddress).address)
        assertEquals("[Naroo] 이메일 인증을 완료해 주세요", sent.subject)
        assertEquals("no-reply@naroo.app", (sent.from.single() as InternetAddress).address)
        assertTrue(sent.content.toString().contains("https://naroo.app/verify-email?token=email-token-1.secret"))
    }
}

private class CapturingJavaMailSender : JavaMailSender {
    val sent = mutableListOf<MimeMessage>()

    override fun createMimeMessage(): MimeMessage {
        return MimeMessage(Session.getInstance(Properties()))
    }

    override fun createMimeMessage(contentStream: java.io.InputStream): MimeMessage {
        return MimeMessage(Session.getInstance(Properties()), contentStream)
    }

    override fun send(mimeMessage: MimeMessage) {
        sent += mimeMessage
    }

    override fun send(vararg mimeMessages: MimeMessage) {
        sent += mimeMessages
    }

    override fun send(mimeMessagePreparator: org.springframework.mail.javamail.MimeMessagePreparator) {
        val message = createMimeMessage()
        mimeMessagePreparator.prepare(message)
        sent += message
    }

    override fun send(vararg mimeMessagePreparators: org.springframework.mail.javamail.MimeMessagePreparator) {
        mimeMessagePreparators.forEach(::send)
    }

    override fun send(simpleMessage: org.springframework.mail.SimpleMailMessage) {
        error("simple mail should not be used")
    }

    override fun send(vararg simpleMessages: org.springframework.mail.SimpleMailMessage) {
        error("simple mail should not be used")
    }
}

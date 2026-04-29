package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingEmailSenderAdapter : EmailSenderPort {
    private val logger = LoggerFactory.getLogger(LoggingEmailSenderAdapter::class.java)

    override fun sendEmailVerification(message: EmailVerificationMessage) {
        logger.info(
            "email verification requested userId={} email={} token={}",
            message.userId,
            message.email,
            message.token,
        )
    }
}

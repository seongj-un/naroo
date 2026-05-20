package com.example.naroo.auth.adapter.`out`.mail

import com.example.naroo.auth.port.`out`.EmailSenderPort
import com.example.naroo.auth.port.`out`.EmailVerificationMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "naroo.auth.email", name = ["mode"], havingValue = "log", matchIfMissing = true)
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

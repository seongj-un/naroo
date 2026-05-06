package com.example.naroo.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.config.annotation.CorsRegistration
import org.springframework.web.servlet.config.annotation.CorsRegistry

class WebCorsConfigTest {
    @Test
    fun `allows only configured origins with credentials for api routes`() {
        val registry = CapturingCorsRegistry()

        WebCorsConfig("https://naroo.app, https://www.naroo.app").addCorsMappings(registry)

        val registration = registry.registration ?: error("cors registration was not captured")
        assertEquals("/api/**", registration.capturedPathPattern)
        assertEquals(listOf("https://naroo.app", "https://www.naroo.app"), registration.allowedOrigins)
        assertEquals(true, registration.allowCredentials)
        assertTrue("OPTIONS" in registration.allowedMethods)
    }
}

private class CapturingCorsRegistry : CorsRegistry() {
    var registration: CapturingCorsRegistration? = null

    override fun addMapping(pathPattern: String): CorsRegistration {
        return CapturingCorsRegistration(pathPattern).also {
            registration = it
        }
    }
}

private class CapturingCorsRegistration(
    val capturedPathPattern: String,
) : CorsRegistration(capturedPathPattern) {
    val allowedOrigins = mutableListOf<String>()
    val allowedMethods = mutableListOf<String>()
    var allowCredentials: Boolean? = null

    override fun allowedOrigins(vararg origins: String): CorsRegistration {
        allowedOrigins += origins
        return this
    }

    override fun allowedMethods(vararg methods: String): CorsRegistration {
        allowedMethods += methods
        return this
    }

    override fun allowedHeaders(vararg headers: String): CorsRegistration {
        return this
    }

    override fun allowCredentials(allowCredentials: Boolean): CorsRegistration {
        this.allowCredentials = allowCredentials
        return this
    }
}

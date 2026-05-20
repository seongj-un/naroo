package com.example.naroo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebCorsConfig(
    @Value("\${naroo.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private val allowedOriginsValue: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        // Keep the temporary Vercel QA frontend reachable until the final custom domain cutover.
        val allowedOrigins = allowedOriginsValue
            .split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .plus(extraAllowedOrigins)
            .distinct()
            .toTypedArray()

        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    companion object {
        private val extraAllowedOrigins = listOf(
            "https://web-naroo.vercel.app",
        )
    }
}

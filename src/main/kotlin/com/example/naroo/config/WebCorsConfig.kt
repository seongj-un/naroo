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
        val allowedOrigins = allowedOriginsValue
            .split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .toTypedArray()

        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}

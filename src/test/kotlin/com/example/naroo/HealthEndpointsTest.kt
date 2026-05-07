package com.example.naroo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "management.health.redis.enabled=false",
    ],
)
class HealthEndpointsTest(
    @LocalServerPort private val port: Int,
) {
    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun `exposes actuator health endpoint on application port`() {
        val actuatorHealth = get("/actuator/health")

        assertEquals(200, actuatorHealth.statusCode())
        assertTrue(actuatorHealth.body().contains("\"status\":\"UP\""))
    }

    private fun get(path: String): HttpResponse<String> {
        return httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI("http://localhost:$port$path"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
    }
}

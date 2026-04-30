package com.example.naroo.content.application.service

import com.example.naroo.content.port.`in`.UpsertRecoveryMissionTemplateContentCommand
import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UpsertRecoveryMissionTemplateContentServiceTest {
    @Test
    fun `upserts recovery mission template content`() {
        val repository = CapturingContentRecoveryMissionTemplateRepository()
        val service = UpsertRecoveryMissionTemplateContentService(repository)

        val result = service.upsert(
            UpsertRecoveryMissionTemplateContentCommand(
                conceptTag = "linear_function_slope",
                title = "기울기 복구",
                prompt = "기울기만 찾아요.",
                hints = listOf("x 앞 숫자를 봐요."),
                estimatedMinutes = 8,
                status = "INACTIVE",
            ),
        )

        assertEquals("linear_function_slope", result.conceptTag)
        assertEquals("INACTIVE", result.status)
        assertEquals(8, repository.saved.single().estimatedMinutes)
    }

    @Test
    fun `rejects empty hints`() {
        val service = UpsertRecoveryMissionTemplateContentService(CapturingContentRecoveryMissionTemplateRepository())

        assertThrows(IllegalArgumentException::class.java) {
            service.upsert(
                UpsertRecoveryMissionTemplateContentCommand(
                    conceptTag = "linear_function_slope",
                    title = "기울기 복구",
                    prompt = "기울기만 찾아요.",
                    hints = emptyList(),
                    estimatedMinutes = 8,
                    status = "ACTIVE",
                ),
            )
        }
    }
}

private class CapturingContentRecoveryMissionTemplateRepository : RecoveryMissionTemplateRepositoryPort {
    val saved = mutableListOf<RecoveryMissionTemplate>()

    override fun findActiveByConceptTag(conceptTag: String): RecoveryMissionTemplate? {
        return saved.firstOrNull { it.conceptTag == conceptTag }
    }

    override fun findAll(): List<RecoveryMissionTemplate> {
        return saved
    }

    override fun save(template: RecoveryMissionTemplate): RecoveryMissionTemplate {
        saved += template
        return template
    }
}

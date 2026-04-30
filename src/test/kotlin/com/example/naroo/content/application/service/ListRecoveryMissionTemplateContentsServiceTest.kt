package com.example.naroo.content.application.service

import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListRecoveryMissionTemplateContentsServiceTest {
    @Test
    fun `lists recovery mission templates for content QA`() {
        val service = ListRecoveryMissionTemplateContentsService(
            recoveryMissionTemplateRepositoryPort = FakeContentRecoveryMissionTemplateRepository(
                templates = listOf(
                    RecoveryMissionTemplate(
                        conceptTag = "linear_function_slope",
                        title = "일차함수 기울기 10분 복구 미션",
                        prompt = "기울기를 찾는 연습만 해요.",
                        hints = listOf("x 앞에 붙은 숫자를 먼저 찾아봐요."),
                        estimatedMinutes = 10,
                    ),
                ),
            ),
        )

        val result = service.list()

        assertEquals("linear_function_slope", result.single().conceptTag)
        assertEquals(10, result.single().estimatedMinutes)
        assertEquals(1, result.single().hints.size)
    }
}

private class FakeContentRecoveryMissionTemplateRepository(
    private val templates: List<RecoveryMissionTemplate>,
) : RecoveryMissionTemplateRepositoryPort {
    override fun findActiveByConceptTag(conceptTag: String): RecoveryMissionTemplate? {
        return templates.firstOrNull { it.conceptTag == conceptTag }
    }

    override fun findAll(): List<RecoveryMissionTemplate> {
        return templates
    }

    override fun save(template: RecoveryMissionTemplate): RecoveryMissionTemplate {
        error("template should not be saved")
    }
}

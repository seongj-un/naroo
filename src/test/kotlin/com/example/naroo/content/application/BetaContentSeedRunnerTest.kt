package com.example.naroo.content.application

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.DefaultApplicationArguments

class BetaContentSeedRunnerTest {
    @Test
    fun `does nothing when seed is disabled`() {
        val questionRepository = CapturingDiagnosticQuestionRepository()
        val templateRepository = CapturingRecoveryMissionTemplateRepository()

        val runner = BetaContentSeedRunner(
            diagnosticQuestionRepositoryPort = questionRepository,
            recoveryMissionTemplateRepositoryPort = templateRepository,
            enabled = false,
        )

        runner.run(DefaultApplicationArguments(*emptyArray<String>()))

        assertEquals(0, questionRepository.saved.size)
        assertEquals(0, templateRepository.saved.size)
    }

    @Test
    fun `seeds beta questions and templates when enabled`() {
        val questionRepository = CapturingDiagnosticQuestionRepository()
        val templateRepository = CapturingRecoveryMissionTemplateRepository()

        val runner = BetaContentSeedRunner(
            diagnosticQuestionRepositoryPort = questionRepository,
            recoveryMissionTemplateRepositoryPort = templateRepository,
            enabled = true,
        )

        runner.run(DefaultApplicationArguments(*emptyArray<String>()))

        assertEquals(BetaContentSeedRunner.BETA_QUESTIONS.size, questionRepository.saved.size)
        assertEquals(BetaContentSeedRunner.BETA_RECOVERY_TEMPLATES.size, templateRepository.saved.size)
        assertEquals(
            setOf(
                MathArea.EQUATION,
                MathArea.FUNCTION,
                MathArea.GEOMETRY,
                MathArea.PROBABILITY_AND_STATISTICS,
                MathArea.SEQUENCE,
            ),
            questionRepository.saved.map { it.mathArea }.toSet(),
        )
    }

    @Test
    fun `updates changed records and skips unchanged records`() {
        val existingQuestions = BetaContentSeedRunner.BETA_QUESTIONS
            .drop(1)
            .toMutableList()
            .apply {
                add(
                    BetaContentSeedRunner.BETA_QUESTIONS.first().copy(
                        prompt = "예전 문항",
                    ),
                )
            }
        val existingTemplates = BetaContentSeedRunner.BETA_RECOVERY_TEMPLATES
            .drop(1)
            .toMutableList()
            .apply {
                add(
                    BetaContentSeedRunner.BETA_RECOVERY_TEMPLATES.first().copy(
                        title = "예전 미션",
                    ),
                )
            }

        val questionRepository = CapturingDiagnosticQuestionRepository(existingQuestions)
        val templateRepository = CapturingRecoveryMissionTemplateRepository(existingTemplates)

        val runner = BetaContentSeedRunner(
            diagnosticQuestionRepositoryPort = questionRepository,
            recoveryMissionTemplateRepositoryPort = templateRepository,
            enabled = true,
        )

        runner.run(DefaultApplicationArguments(*emptyArray<String>()))

        assertEquals(1, questionRepository.saved.size)
        assertEquals(
            DiagnosticQuestionId("equation-balance-1"),
            questionRepository.saved.single().id,
        )
        assertEquals(1, templateRepository.saved.size)
        assertEquals("equation_balance", templateRepository.saved.single().conceptTag)
    }
}

private class CapturingDiagnosticQuestionRepository(
    initial: List<DiagnosticQuestion> = emptyList(),
) : DiagnosticQuestionRepositoryPort {
    val saved = mutableListOf<DiagnosticQuestion>()
    private val questions = initial.associateBy { it.id.value }.toMutableMap()

    override fun findActiveByMathArea(mathArea: MathArea): List<DiagnosticQuestion> {
        return questions.values.filter { it.mathArea == mathArea }
    }

    override fun findAll(): List<DiagnosticQuestion> {
        return questions.values.sortedBy { it.id.value }
    }

    override fun save(question: DiagnosticQuestion): DiagnosticQuestion {
        saved += question
        questions[question.id.value] = question
        return question
    }
}

private class CapturingRecoveryMissionTemplateRepository(
    initial: List<RecoveryMissionTemplate> = emptyList(),
) : RecoveryMissionTemplateRepositoryPort {
    val saved = mutableListOf<RecoveryMissionTemplate>()
    private val templates = initial.associateBy { it.conceptTag }.toMutableMap()

    override fun findActiveByConceptTag(conceptTag: String): RecoveryMissionTemplate? {
        return templates[conceptTag]
    }

    override fun findAll(): List<RecoveryMissionTemplate> {
        return templates.values.sortedBy { it.conceptTag }
    }

    override fun save(template: RecoveryMissionTemplate): RecoveryMissionTemplate {
        saved += template
        templates[template.conceptTag] = template
        return template
    }
}

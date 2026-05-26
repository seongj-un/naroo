package com.example.naroo.content.application

import com.example.naroo.diagnostic.domain.DiagnosticQuestion
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoice
import com.example.naroo.diagnostic.domain.DiagnosticQuestionChoiceId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionId
import com.example.naroo.diagnostic.domain.DiagnosticQuestionStatus
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticQuestionRepositoryPort
import com.example.naroo.recovery.application.service.RecoveryMissionTemplate
import com.example.naroo.recovery.application.service.RecoveryMissionTemplateStatus
import com.example.naroo.recovery.port.`out`.RecoveryMissionTemplateRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class BetaContentSeedRunner(
    private val diagnosticQuestionRepositoryPort: DiagnosticQuestionRepositoryPort,
    private val recoveryMissionTemplateRepositoryPort: RecoveryMissionTemplateRepositoryPort,
    @Value("\${naroo.seed.beta-content.enabled:false}") private val enabled: Boolean,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!enabled) {
            return
        }

        seedQuestions()
        seedRecoveryMissionTemplates()

        logger.info(
            "beta content seed applied questionCount={} templateCount={}",
            BETA_QUESTIONS.size,
            BETA_RECOVERY_TEMPLATES.size,
        )
    }

    private fun seedQuestions() {
        val existingById = diagnosticQuestionRepositoryPort.findAll().associateBy { it.id.value }
        BETA_QUESTIONS.forEach { question ->
            val existing = existingById[question.id.value]
            if (existing != question) {
                diagnosticQuestionRepositoryPort.save(question)
            }
        }
    }

    private fun seedRecoveryMissionTemplates() {
        val existingByConceptTag = recoveryMissionTemplateRepositoryPort.findAll().associateBy { it.conceptTag }
        BETA_RECOVERY_TEMPLATES.forEach { template ->
            val existing = existingByConceptTag[template.conceptTag]
            if (existing != template) {
                recoveryMissionTemplateRepositoryPort.save(template)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BetaContentSeedRunner::class.java)

        internal val BETA_QUESTIONS = listOf(
            diagnosticQuestion(
                id = "equation-balance-1",
                mathArea = MathArea.EQUATION,
                prompt = "2x + 5 = 17일 때 x의 값은?",
                conceptTag = "equation_balance",
                displayOrder = 1,
                answers = listOf("5", "6", "11"),
                correctChoiceId = "b",
            ),
            diagnosticQuestion(
                id = "equation-distribute-1",
                mathArea = MathArea.EQUATION,
                prompt = "3(x + 2) = 18일 때 x의 값은?",
                conceptTag = "equation_distribution",
                displayOrder = 2,
                answers = listOf("4", "5", "6"),
                correctChoiceId = "a",
            ),
            diagnosticQuestion(
                id = "function-substitution-1",
                mathArea = MathArea.FUNCTION,
                prompt = "함수 y = 2x + 1에서 x가 3일 때 y의 값은?",
                conceptTag = "function_substitution",
                displayOrder = 1,
                answers = listOf("5", "7", "9"),
                correctChoiceId = "b",
            ),
            diagnosticQuestion(
                id = "function-slope-1",
                mathArea = MathArea.FUNCTION,
                prompt = "일차함수 y = -3x + 2의 기울기는?",
                conceptTag = "linear_function_slope",
                displayOrder = 2,
                answers = listOf("-3", "2", "3"),
                correctChoiceId = "a",
            ),
            diagnosticQuestion(
                id = "geometry-angle-sum-1",
                mathArea = MathArea.GEOMETRY,
                prompt = "삼각형의 두 각이 35도, 65도일 때 나머지 한 각의 크기는?",
                conceptTag = "triangle_angle_sum",
                displayOrder = 1,
                answers = listOf("70도", "80도", "90도"),
                correctChoiceId = "b",
            ),
            diagnosticQuestion(
                id = "geometry-coordinate-distance-1",
                mathArea = MathArea.GEOMETRY,
                prompt = "좌표평면에서 점 (2, 1)과 (2, 6)의 거리는?",
                conceptTag = "coordinate_distance",
                displayOrder = 2,
                answers = listOf("3", "4", "5"),
                correctChoiceId = "c",
            ),
            diagnosticQuestion(
                id = "probability-sample-space-1",
                mathArea = MathArea.PROBABILITY_AND_STATISTICS,
                prompt = "동전을 두 번 던질 때 가능한 결과는 모두 몇 가지인가요?",
                conceptTag = "sample_space_count",
                displayOrder = 1,
                answers = listOf("2가지", "3가지", "4가지"),
                correctChoiceId = "c",
            ),
            diagnosticQuestion(
                id = "statistics-mean-1",
                mathArea = MathArea.PROBABILITY_AND_STATISTICS,
                prompt = "3, 5, 7, 9의 평균은?",
                conceptTag = "mean_interpretation",
                displayOrder = 2,
                answers = listOf("5", "6", "7"),
                correctChoiceId = "b",
            ),
            diagnosticQuestion(
                id = "sequence-pattern-1",
                mathArea = MathArea.SEQUENCE,
                prompt = "2, 5, 8, 11, ... 의 다음 수는?",
                conceptTag = "sequence_common_difference",
                displayOrder = 1,
                answers = listOf("12", "13", "14"),
                correctChoiceId = "c",
            ),
            diagnosticQuestion(
                id = "sequence-sum-1",
                mathArea = MathArea.SEQUENCE,
                prompt = "1부터 5까지의 합은?",
                conceptTag = "sequence_sum_basics",
                displayOrder = 2,
                answers = listOf("10", "15", "20"),
                correctChoiceId = "b",
            ),
        )

        internal val BETA_RECOVERY_TEMPLATES = listOf(
            recoveryMissionTemplate(
                conceptTag = "equation_balance",
                title = "등식의 균형 다시 잡기",
                prompt = "식의 양쪽에서 같은 수를 더하거나 빼도 등식은 유지돼요. 각 단계에서 무엇을 없애고 싶은지 먼저 말로 적어보세요.",
                hints = listOf(
                    "x가 붙지 않은 숫자부터 반대 연산으로 옮겨보세요.",
                    "한 줄마다 양쪽에 같은 연산을 했는지 확인하세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "equation_distribution",
                title = "분배법칙 다시 연결하기",
                prompt = "괄호 앞 숫자가 괄호 안 모든 항에 곱해진다는 감각을 먼저 회복해볼게요.",
                hints = listOf(
                    "3(x + 2)는 3*x + 3*2로 펼쳐져요.",
                    "괄호를 푼 뒤에는 일차방정식처럼 한 단계씩 정리해 보세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "function_substitution",
                title = "함수값 대입 다시 해보기",
                prompt = "x 자리에 숫자를 넣은 뒤, 계산 순서를 천천히 따라가며 함수값을 구해보세요.",
                hints = listOf(
                    "x가 3이면 식의 x를 모두 3으로 바꿔 적어보세요.",
                    "곱셈을 먼저 계산한 뒤 더하기를 해보세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "linear_function_slope",
                title = "기울기 감각 다시 세우기",
                prompt = "일차함수에서 x 앞의 숫자가 그래프의 기울기라는 연결을 다시 잡아볼게요.",
                hints = listOf(
                    "y = ax + b 꼴에서 a가 기울기예요.",
                    "기울기가 음수면 x가 커질수록 y는 내려가요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "triangle_angle_sum",
                title = "삼각형 각의 합 다시 확인하기",
                prompt = "삼각형의 세 각의 합은 항상 180도예요. 알고 있는 각 둘을 먼저 더해보세요.",
                hints = listOf(
                    "35도와 65도를 먼저 더해보세요.",
                    "180도에서 이미 알고 있는 합을 빼면 남은 각이 나와요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "coordinate_distance",
                title = "좌표 사이 거리 읽기",
                prompt = "x좌표가 같으면 세로 거리만 보면 돼요. 위아래로 몇 칸 차이 나는지 세어보세요.",
                hints = listOf(
                    "두 점의 x좌표가 같으면 수직선 위 차이만 보면 돼요.",
                    "1에서 6까지 얼마나 떨어졌는지 생각해 보세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "sample_space_count",
                title = "경우의 수 펼쳐 보기",
                prompt = "가능한 결과를 빠뜨리지 않게 하나씩 써보면 확률 문제가 훨씬 쉬워져요.",
                hints = listOf(
                    "앞면/뒷면을 첫 번째와 두 번째 던짐으로 나눠 적어보세요.",
                    "HH, HT, TH, TT처럼 순서를 포함해 세어보세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "mean_interpretation",
                title = "평균 계산 다시 정리하기",
                prompt = "평균은 전체를 더한 뒤 개수로 나누는 과정이에요. 두 단계를 분리해서 적어보세요.",
                hints = listOf(
                    "3 + 5 + 7 + 9를 먼저 계산해보세요.",
                    "숫자가 4개이므로 합을 4로 나눠야 해요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "sequence_common_difference",
                title = "수열의 차이 찾기",
                prompt = "앞뒤 항이 얼마나 일정하게 늘어나는지 보면 규칙이 보여요.",
                hints = listOf(
                    "5-2, 8-5, 11-8을 각각 계산해 보세요.",
                    "같은 만큼 늘어나면 그 수를 다음 항에도 더해보세요.",
                ),
            ),
            recoveryMissionTemplate(
                conceptTag = "sequence_sum_basics",
                title = "작은 합부터 다시 묶기",
                prompt = "1부터 5까지를 차례대로 더하면서, 중간 합을 적어보면 실수가 줄어요.",
                hints = listOf(
                    "1+2=3, 3+3=6처럼 중간값을 써보세요.",
                    "끝 숫자까지 모두 더했는지 마지막에 다시 확인하세요.",
                ),
            ),
        )

        private fun diagnosticQuestion(
            id: String,
            mathArea: MathArea,
            prompt: String,
            conceptTag: String,
            displayOrder: Int,
            answers: List<String>,
            correctChoiceId: String,
        ): DiagnosticQuestion {
            return DiagnosticQuestion(
                id = DiagnosticQuestionId(id),
                mathArea = mathArea,
                prompt = prompt,
                choices = listOf(
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("a"), answers[0]),
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("b"), answers[1]),
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("c"), answers[2]),
                    DiagnosticQuestionChoice(DiagnosticQuestionChoiceId("unknown"), "잘 모르겠음"),
                ),
                correctChoiceId = DiagnosticQuestionChoiceId(correctChoiceId),
                conceptTag = conceptTag,
                displayOrder = displayOrder,
                status = DiagnosticQuestionStatus.ACTIVE,
            )
        }

        private fun recoveryMissionTemplate(
            conceptTag: String,
            title: String,
            prompt: String,
            hints: List<String>,
        ): RecoveryMissionTemplate {
            return RecoveryMissionTemplate(
                conceptTag = conceptTag,
                title = title,
                prompt = prompt,
                hints = hints,
                estimatedMinutes = 10,
                status = RecoveryMissionTemplateStatus.ACTIVE,
            )
        }
    }
}

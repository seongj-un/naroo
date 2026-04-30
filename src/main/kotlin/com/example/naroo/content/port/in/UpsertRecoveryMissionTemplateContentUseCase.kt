package com.example.naroo.content.port.`in`

fun interface UpsertRecoveryMissionTemplateContentUseCase {
    fun upsert(command: UpsertRecoveryMissionTemplateContentCommand): RecoveryMissionTemplateContentResult
}

data class UpsertRecoveryMissionTemplateContentCommand(
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val estimatedMinutes: Int,
    val status: String,
)

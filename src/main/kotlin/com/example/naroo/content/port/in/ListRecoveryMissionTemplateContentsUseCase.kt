package com.example.naroo.content.port.`in`

fun interface ListRecoveryMissionTemplateContentsUseCase {
    fun list(): List<RecoveryMissionTemplateContentResult>
}

data class RecoveryMissionTemplateContentResult(
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val estimatedMinutes: Int,
    val status: String,
)

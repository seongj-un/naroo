package com.example.naroo.recovery.application.service

data class RecoveryMissionTemplate(
    val conceptTag: String,
    val title: String,
    val prompt: String,
    val hints: List<String>,
    val estimatedMinutes: Int = 10,
)

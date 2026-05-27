package com.example.naroo.betaops.domain

enum class BetaOpsMode {
    DISABLED,
    SHADOW,
    ENABLED,
    ;

    fun allowsEventIngest(): Boolean {
        return this != DISABLED
    }

    companion object {
        fun from(value: String): BetaOpsMode {
            return when (value.trim().lowercase()) {
                "disabled" -> DISABLED
                "shadow" -> SHADOW
                "enabled" -> ENABLED
                else -> throw IllegalArgumentException(
                    "naroo.beta-ops.mode must be one of disabled, shadow, enabled",
                )
            }
        }
    }
}

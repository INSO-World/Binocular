package com.inso_world.binocular.model

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime

data class Signature(
    @field:NotNull
    val developerId: Developer.Id,
    @field:NotNull
    val gitSignature: String,
    @field:PastOrPresent
    @field:NotNull
    val timestamp: LocalDateTime
) {
    init {
        val now = LocalDateTime.now().plusNanos(1)
        require(timestamp.isBefore(now)) {
            "timestamp ($timestamp) must be past or present ($now)"
        }
    }

    fun toDeveloper(): Developer = Developer(name = name, email = email)

    private val name: String
        get() {
            val nameRegex = Regex("""^(.+?)\s*<""")
            return nameRegex
                .find(gitSignature)
                ?.groupValues
                ?.get(1)
                ?.trim() ?: "Unknown"
        }

    private val email: String
        get() {
            val emailRegex = Regex("""<([^>]+)>$""")
            return emailRegex.find(gitSignature)?.groupValues?.get(1) ?: "unknown@example.com"
        }
}

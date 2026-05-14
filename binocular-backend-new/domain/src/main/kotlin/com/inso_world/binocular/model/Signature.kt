package com.inso_world.binocular.model

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
}

package com.inso_world.binocular.model

import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a Build, representing a CI/CD build.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Build(
    var id: String? = null,
    var sha: String? = null,
    var ref: String? = null,
    var status: String? = null,
    var tag: String? = null,
    var user: String? = null,
    var userFullName: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var committedAt: LocalDateTime? = null,
    var duration: Int? = null,
    var jobs: List<Job> = emptyList(),
    var webUrl: String? = null,
    // Relationships
    var commits: List<Commit> = emptyList(),
) : AbstractDomainObject<Build.Id, Build.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    // TODO work in progress, just for compatibility
    data class Key(val key: String) // value object for lookups

    override val uniqueKey: Key
        get() = TODO("Not yet implemented")
}

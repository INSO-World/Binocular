package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a Milestone, representing a milestone in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Milestone(
    var id: String? = null,
    var platformIid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var startDate: String? = null,
    var dueDate: String? = null,
    var state: String? = null,
    var expired: Boolean? = null,
    var webUrl: String? = null,
    // Relationships
    var issues: List<Issue> = emptyList(),
    var mergeRequests: List<MergeRequest> = emptyList(),
) : AbstractDomainObject<Milestone.Id, Milestone.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    // TODO work in progress, just for compatibility
    data class Key(val key: String) // value object for lookups

    override val uniqueKey: Key
        get() = TODO("Not yet implemented")
}

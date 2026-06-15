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
    val project: Project.Id,
    val issues: MutableSet<Issue> = NonRemovingMutableSet(),
    val mergeRequests: MutableSet<MergeRequest> = NonRemovingMutableSet(),
) : AbstractDomainObject<Milestone.Id, Milestone.Key>(
        Id(Uuid.random())
    ) {
    @JvmInline
    value class Id(
        val value: Uuid
    )

    data class Key(
        val projectId: Project.Id,
        val platformIid: Int?
    ) // value object for lookups

    override val uniqueKey: Key
        get() = Key(project, platformIid)
}

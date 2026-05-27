package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a MergeRequest, representing a merge/pull request in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class MergeRequest(
    var id: String? = null,
    var platformIid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: String? = null,
    var closedAt: String? = null,
    var updatedAt: String? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<Mention> = emptyList(),
    // Relationships
    val project: Project.Id,
    var accounts: MutableList<Account> = mutableListOf(),
    var milestones: List<Milestone> = emptyList(),
    var notes: List<Note> = emptyList(),
): AbstractDomainObject<MergeRequest.Id, MergeRequest.Key>(
    Id(Uuid.random())
){
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val projectId: Project.Id, val platformIid: Int?) // value object for lookups

    override val uniqueKey: Key
        get() = Key(project, platformIid)
}

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
    var accounts: List<Account> = emptyList(),
    var milestones: List<Milestone> = emptyList(),
    var notes: List<Note> = emptyList(),
): AbstractDomainObject<MergeRequest.Id, MergeRequest.Key>(
    Id(Uuid.random())
){
    @JvmInline
    value class Id(val value: Uuid)

    // TODO work in progress, just for compatibility
    data class Key(val key: String) // value object for lookups

    override val uniqueKey: Key
        get() = TODO("Not yet implemented")
}

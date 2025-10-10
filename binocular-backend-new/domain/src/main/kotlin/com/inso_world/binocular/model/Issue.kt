package com.inso_world.binocular.model

import java.time.LocalDateTime

/**
 * Domain model for an Issue, representing an issue in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Issue(
    var id: String? = null,
    var iid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: LocalDateTime? = null,
    var closedAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<Mention> = emptyList(),
    // Relationships
    var accounts: List<Account> = emptyList(),
    var commits: List<Commit> = emptyList(),
    var milestones: List<Milestone> = emptyList(),
    var notes: List<Note> = emptyList(),
    var users: List<User> = emptyList(),
)

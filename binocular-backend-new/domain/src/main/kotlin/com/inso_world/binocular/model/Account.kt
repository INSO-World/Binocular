package com.inso_world.binocular.model

/**
 * Domain model for an Account, representing a user account from a platform like GitHub or GitLab.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Account(
    var id: String? = null,
    var platform: Platform? = null,
    var login: String? = null,
    var name: String? = null,
    var avatarUrl: String? = null,
    var url: String? = null,
    // Relationships
    var issues: List<Issue> = emptyList(),
    var mergeRequests: List<MergeRequest> = emptyList(),
    var notes: List<Note> = emptyList(),
    var users: List<User> = emptyList(),
)

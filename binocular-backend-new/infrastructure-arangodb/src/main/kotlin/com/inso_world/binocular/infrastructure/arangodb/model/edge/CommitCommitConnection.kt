package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Commit

/**
 * Domain model for a connection between two Commits.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitCommitConnection(
    var id: String? = null,
    var from: Commit,
    var to: Commit,
)

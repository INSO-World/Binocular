package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Commit

/**
 * Domain model for a connection between a Commit and a Module.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitModuleConnection(
    var id: String? = null,
    var from: Commit,
    var to: com.inso_world.binocular.model.Module,
)

package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.User

/**
 * Domain model for a connection between a File and a User.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitFileUserConnection(
    var id: String? = null,
    var from: File,
    var to: User,
)

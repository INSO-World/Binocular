package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.File

/**
 * Domain model for a connection between a Module and a File.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class ModuleFileConnection(
    var id: String? = null,
    var from: com.inso_world.binocular.model.Module,
    var to: File,
)

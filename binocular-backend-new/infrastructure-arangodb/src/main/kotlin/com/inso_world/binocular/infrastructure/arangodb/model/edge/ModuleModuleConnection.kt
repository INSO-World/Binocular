package com.inso_world.binocular.infrastructure.arangodb.model.edge

/**
 * Domain model for a connection between two Modules.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class ModuleModuleConnection(
    var id: String? = null,
    var from: com.inso_world.binocular.model.Module,
    var to: com.inso_world.binocular.model.Module,
)

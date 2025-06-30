package com.inso_world.binocular.web.persistence.entity.arangodb.edges

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between two Modules.
 */
@Edge(value = "modules-modules")
data class ModuleModuleConnectionEntity(
    @Id var id: String? = null,
    @From var from: ModuleEntity,
    @To var to: ModuleEntity
)

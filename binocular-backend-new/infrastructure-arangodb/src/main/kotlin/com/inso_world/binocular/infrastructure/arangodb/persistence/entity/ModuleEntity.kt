package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitModuleConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.ModuleFileConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.ModuleModuleConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Module entity.
 */
@Document("modules")
data class ModuleEntity(
    @Id
    var id: String? = null,
    var path: String,
    @Relations(
        edges = [CommitModuleConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var commits: List<CommitEntity> = emptyList(),
    @Relations(
        edges = [ModuleFileConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var files: List<FileEntity> = emptyList(),
    @Relations(
        edges = [ModuleModuleConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var childModules: List<ModuleEntity> = emptyList(),
    @Relations(
        edges = [ModuleModuleConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var parentModules: List<ModuleEntity> = emptyList(),
)

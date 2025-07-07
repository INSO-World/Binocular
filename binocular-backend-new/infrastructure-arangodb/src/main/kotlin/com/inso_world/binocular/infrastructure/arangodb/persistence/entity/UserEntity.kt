package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific User entity.
 */
@Document("users")
data class UserEntity(
    @Id
    var id: String? = null,
    var gitSignature: String,
    @Relations(
        edges = [CommitUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var commits: List<CommitEntity> = emptyList(),
    @Relations(
        edges = [IssueUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var issues: List<IssueEntity> = emptyList(),
    @Relations(
        edges = [CommitFileUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var files: List<FileEntity> = emptyList(),
)

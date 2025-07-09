package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.*
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitBuildConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitCommitConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitModuleConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueCommitConnectionEntity
import com.inso_world.binocular.model.*
import org.springframework.data.annotation.Id
import java.util.Date

/**
 * ArangoDB-specific Commit entity.
 */
@Document(collection = "commits")
data class CommitEntity(
    @Id var id: String? = null,
    @Field("sha")
    @PersistentIndexed(unique = true)
    var sha: String,
    var date: Date? = null,
    var message: String? = null,
    var webUrl: String? = null,
    var branch: String? = null,
    var stats: Stats? = null,
    @Relations(edges = [CommitCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
    var parents: List<CommitEntity> = emptyList(),
    @Relations(edges = [CommitCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
    var children: List<CommitEntity> = emptyList(),
    @Relations(edges = [CommitBuildConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
    var builds: List<BuildEntity> = emptyList(),
    @Relations(edges = [CommitFileConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
    var files: List<FileEntity> = emptyList(),
    @Relations(edges = [CommitModuleConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
    var modules: List<ModuleEntity> = emptyList(),
    @Relations(edges = [CommitUserConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.OUTBOUND)
    var users: List<UserEntity> = emptyList(),
    @Relations(edges = [IssueCommitConnectionEntity::class], lazy = true, maxDepth = 1, direction = Relations.Direction.INBOUND)
    var issues: List<IssueEntity> = emptyList(),
)

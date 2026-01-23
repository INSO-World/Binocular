package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitBuildConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitCommitConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitModuleConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueCommitConnectionEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.springframework.data.annotation.Id
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ArangoDB-specific Commit entity.
 */
@OptIn(ExperimentalUuidApi::class)
@Document(collection = "commits")
data class CommitEntity(
    @Id var id: String? = null,
    @Field("sha")
    @PersistentIndexed(unique = true)
    var sha: String,
    var iid: Uuid,
    var date: Date? = null,
    var message: String? = null,
    var webUrl: String? = null,
    var branch: String? = null,
    var stats: StatsEntity? = null,
    @Relations(
        edges = [CommitCommitConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND
    )
    var parents: List<CommitEntity> = emptyList(),
    @Relations(
        edges = [CommitCommitConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND
    )
    var children: List<CommitEntity> = emptyList(),
    @Relations(
        edges = [CommitBuildConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND
    )
    var builds: List<BuildEntity> = emptyList(),
    @Relations(
        edges = [CommitFileConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND
    )
    var files: List<FileEntity> = emptyList(),
    @Relations(
        edges = [CommitModuleConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND
    )
    var modules: List<ModuleEntity> = emptyList(),
    @Relations(
        edges = [CommitUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND
    )
    var users: List<UserEntity> = emptyList(),
    @Relations(
        edges = [IssueCommitConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND
    )
    var issues: List<IssueEntity> = emptyList(),
    @Ref(lazy = false)
    val repository: RepositoryEntity
)

@OptIn(ExperimentalUuidApi::class)
internal fun Commit.toEntity(
    repository: RepositoryEntity,
    author: DeveloperEntity,
    committer: DeveloperEntity,
): CommitEntity =
    CommitEntity(
        iid = this.iid.value,
        sha = this.sha,
//        authorDateTime = this.authorSignature.timestamp,
//        commitDateTime = (this.committerSignature ?: this.authorSignature).timestamp,
        message = this.message,
        webUrl = this.webUrl,
        repository = repository,
//        parents = mutableSetOf(),
//        children = mutableSetOf(),
//        author = author,
//        committer = committer,
    ).apply {
        this.id = this@toEntity.id?.trim()
    }

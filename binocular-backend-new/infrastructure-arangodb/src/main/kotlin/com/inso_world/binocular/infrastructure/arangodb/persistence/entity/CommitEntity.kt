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
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Document(collection = "commits")
data class CommitEntity(
    @Id var id: String? = null,
    @Field("sha")
    @PersistentIndexed(unique = true)
    var sha: String,
    var iid: Uuid,
    var authorDateTime: LocalDateTime,
    var commitDateTime: LocalDateTime,
    @Deprecated("Use authorDateTime/commitDateTime instead")
    var date: Date? = null,
    var message: String? = null,
    var webUrl: String? = null,
    @Deprecated("do not use")
    var branch: String? = null,
    var stats: StatsEntity? = null,
    @Ref(lazy = false)
    val authorId: String,
    @Ref(lazy = false)
    val committerId: String,
    @Ref(lazy = false)
    val repositoryId: String,
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
) {
    fun toDomain(
        repositoryId: Repository.Id,
        authorSignature: Signature,
        committerSignature: Signature,
    ): Commit {
        return Commit(
            sha = this.sha,
            authorSignature = authorSignature,
            committerSignature = committerSignature,
            repositoryId = repositoryId,
            message = this.message,
        ).apply {
            this.id = this@CommitEntity.id
            this.webUrl = this@CommitEntity.webUrl
            this.stats = this@CommitEntity.stats?.let {
                com.inso_world.binocular.model.Stats(
                    additions = it.additions,
                    deletions = it.deletions,
                    kind = it.kind ?: com.inso_world.binocular.model.Stats.StatsKind.MODIFICATION
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun Commit.toEntity(
    repositoryEntity: RepositoryEntity,
    authorEntity: DeveloperEntity,
    committerEntity: DeveloperEntity,
): CommitEntity =
    CommitEntity(
        iid = this.iid.value,
        sha = this.sha,
        authorDateTime = this.authorSignature.timestamp,
        commitDateTime = this.committerSignature.timestamp,
        message = this.message,
        webUrl = this.webUrl,
        repositoryId = repositoryEntity.id ?: throw IllegalStateException("RepositoryEntity must be saved"),
        authorId = authorEntity.id ?: throw IllegalStateException("DeveloperEntity must be saved"),
        committerId = committerEntity.id ?: throw IllegalStateException("DeveloperEntity must be saved"),
        stats = this.stats?.let {
            StatsEntity(
                additions = it.additions.toLong(),
                deletions = it.deletions.toLong(),
                kind = it.kind
            )
        }
    ).apply {
        this.id = this@toEntity.id?.trim()
    }

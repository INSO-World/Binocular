package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.BranchFileConnectionEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ArangoDB-specific Branch entity.
 *
 * Represents the persistence layer for the [Branch][com.inso_world.binocular.model.Branch] domain object.
 *
 * ### Identity Mapping
 * - [id]: ArangoDB internal document ID (_key)
 * - [iid]: Domain immutable identity (UUID)
 * - [name]: Branch name (business key component with repository)
 *
 * ### Relationships
 * - [repository]: Owning repository (required)
 * - [head]: Head commit reference (required)
 * - [files]: Related files via edge collection
 *
 * ### Indexes
 * - [iid]: Unique persistent index for UUID-based lookups
 */
@OptIn(ExperimentalUuidApi::class)
@Document("branches")
data class BranchEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var name: String,
    var fullName: String,
    var category: String,
    var active: Boolean = false,
    var tracksFileRenames: Boolean = false,
    @Deprecated("Use head.sha instead")
    var latestCommit: String? = null,
    @Ref(lazy = false)
    val repositoryId: String,
    @Ref(lazy = false)
    val headCommitId: String,
    @Relations(
        edges = [BranchFileConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var files: Set<FileEntity> = emptySet(),
) {

    @Deprecated("Legacy", replaceWith = ReplaceWith("fullName"))
    val branch = fullName

    /**
     * Converts this BranchEntity to a Branch domain object.
     *
     * @param repositoryId The repository ID to associate with the branch
     * @param headCommitId The head commit ID
     * @return Branch domain object
     */
    fun toDomain(repositoryId: Repository.Id, headCommitId: Commit.Id): Branch {
        return Branch(
            name = this.name,
            fullName = this.fullName,
            category = ReferenceCategory.valueOf(this.category),
            repositoryId = repositoryId,
            headCommitId = headCommitId,
        ).apply {
            this.id = this@BranchEntity.id
            this.active = this@BranchEntity.active
            this.tracksFileRenames = this@BranchEntity.tracksFileRenames
        }
    }
}

/**
 * Converts a Branch domain object to BranchEntity.
 *
 * @param repositoryEntity The RepositoryEntity to associate with the branch
 * @param headCommitEntity The head CommitEntity
 * @return BranchEntity for persistence
 */
@OptIn(ExperimentalUuidApi::class)
internal fun Branch.toEntity(repositoryEntity: RepositoryEntity, headCommitEntity: CommitEntity): BranchEntity =
    BranchEntity(
        id = this.id,
        iid = this.iid.value,
        name = this.name,
        fullName = this.fullName,
        category = this.category.name,
        active = this.active,
        tracksFileRenames = this.tracksFileRenames,
        latestCommit = this.headCommitId.value.toString(),
        repositoryId = repositoryEntity.id ?: throw IllegalStateException("RepositoryEntity must be saved"),
        headCommitId = headCommitEntity.sha,
    )

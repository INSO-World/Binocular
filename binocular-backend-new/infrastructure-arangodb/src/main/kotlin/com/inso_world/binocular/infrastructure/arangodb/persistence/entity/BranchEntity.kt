package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.BranchFileConnectionEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.springframework.data.annotation.Id
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
@Document("branches")
data class BranchEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid = Uuid.random(),
    var name: String = "",
    var fullName: String = "",
    var category: String = ReferenceCategory.LOCAL_BRANCH.name,
    var active: Boolean = false,
    var tracksFileRenames: Boolean = false,
    @Deprecated("Use head.sha instead")
    var latestCommit: String? = null,
    var branch: String? = null,
    @Ref(lazy = false)
    var repository: RepositoryEntity? = null,
    @Ref(lazy = false)
    var head: CommitEntity? = null,
    @Relations(
        edges = [BranchFileConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var files: Set<FileEntity> = emptySet(),
) {
    /**
     * Converts this BranchEntity to a Branch domain object.
     *
     * @param repository The repository domain object to associate with the branch
     * @param head The head commit domain object
     * @return Branch domain object
     */
    fun toDomain(): Branch {
        val effectiveName = if (this.name.isNotEmpty()) this.name else (this.branch ?: "")
        val effectiveFullName = if (this.fullName.isNotEmpty()) this.fullName else effectiveName
        val effectiveCategory = runCatching { ReferenceCategory.valueOf(this.category) }.getOrDefault(ReferenceCategory.LOCAL_BRANCH)
        val repoIid = this.repository?.iid ?: Uuid.random()
        val headSha = this.head?.sha ?: this.latestCommit ?: ""

        return Branch(
            name = effectiveName,
            fullName = effectiveFullName,
            category = effectiveCategory,
            repositoryId = Repository.Id(repoIid),
            headSha = headSha,
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
 * @param repository The RepositoryEntity to associate with the branch
 * @param head The head CommitEntity
 * @return BranchEntity for persistence
 */
internal fun Branch.toArangoEntity(
    repository: RepositoryEntity,
    head: CommitEntity
): BranchEntity =
    BranchEntity(
        id = this.id,
        iid = this.iid.value,
        name = this.name,
        fullName = this.fullName,
        category = this.category.name,
        active = this.active,
        tracksFileRenames = this.tracksFileRenames,
        latestCommit = this.headSha,
        branch = this.name,
        repository = repository,
        head = head,
    )

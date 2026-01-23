package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ArangoDB-specific User entity.
 *
 * @deprecated Use [DeveloperEntity] instead. This entity is maintained for backwards compatibility.
 *
 * Represents the persistence layer for the [User][com.inso_world.binocular.model.User] domain object.
 *
 * ### Identity Mapping
 * - [id]: ArangoDB internal document ID (_key)
 * - [iid]: Domain immutable identity (UUID)
 * - [gitSignature]: Business key component along with repository
 *
 * ### Relationships
 * - [repository]: Owning repository (required)
 *
 * ### Indexes
 * - [iid]: Unique persistent index for UUID-based lookups
 */
@Deprecated("Use DeveloperEntity instead")
@OptIn(ExperimentalUuidApi::class)
@Document("users")
data class UserEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var gitSignature: String,
    @Ref(lazy = true)
    val repository: RepositoryEntity,
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
    var issues: Set<IssueEntity> = emptySet(),
    @Relations(
        edges = [CommitFileUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.INBOUND,
    )
    var files: Set<FileEntity> = emptySet(),
) {
    val name: String
        get() {
            val nameRegex = Regex("""^(.+?)\s*<""")
            return nameRegex.find(gitSignature)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("could not extract email from gitSignature")
        }

    val email: String
        get() {
            val emailRegex = Regex("""<([^>]+)>$""")
            return emailRegex.find(gitSignature)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("could not extract email from gitSignature")
        }
}

package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ArangoDB-specific Repository entity.
 *
 * Represents the persistence layer for the [Repository][com.inso_world.binocular.model.Repository] domain object.
 *
 * ### Identity Mapping
 * - [id]: ArangoDB internal document ID (_key)
 * - [iid]: Domain immutable identity (UUID)
 * - [localPath]: Business key component along with project
 *
 * ### Relationships
 * - [project]: Owning project. Declared as a nullable body property rather than a constructor
 *   parameter because Spring Data ArangoDB deserializes lazy `@Ref` fields via property injection
 *   after construction — a lazy reference passed as a constructor argument would not yet be
 *   resolved at that point. Nullable also accommodates bulk-seeding flows (e.g. shallow
 *   `ProjectInfrastructurePortImpl.saveAll`) that assemble a [RepositoryEntity] before its owning
 *   [ProjectEntity] is persisted. Set via the [toEntity] factory before use.
 *
 * ### Indexes
 * - [iid]: Unique persistent index for UUID-based lookups
 */
@OptIn(ExperimentalUuidApi::class)
@Document("repositories")
data class RepositoryEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var localPath: String
) {
    @Ref(lazy = true)
    var project: ProjectEntity? = null

    /**
     * Converts this RepositoryEntity to a Repository domain object.
     *
     * @param project The project domain object to associate with the repository
     * @return Repository domain object
     */
    fun toDomain(project: com.inso_world.binocular.model.Project): com.inso_world.binocular.model.Repository =
        com.inso_world.binocular.model
            .Repository(
                localPath = this.localPath.trim(),
                project = project,
            ).apply {
                this.id = this@RepositoryEntity.id
            }
}

/**
 * Converts a Repository domain object to RepositoryEntity.
 *
 * @param project The ProjectEntity to associate with the repository
 * @return RepositoryEntity for persistence
 */
@OptIn(ExperimentalUuidApi::class)
internal fun com.inso_world.binocular.model.Repository.toEntity(project: ProjectEntity): RepositoryEntity =
    RepositoryEntity(
        id = this.id,
        iid = this.iid.value,
        localPath = this.localPath.trim(),
    ).also {
        it.project = project
        project.repository = it
    }

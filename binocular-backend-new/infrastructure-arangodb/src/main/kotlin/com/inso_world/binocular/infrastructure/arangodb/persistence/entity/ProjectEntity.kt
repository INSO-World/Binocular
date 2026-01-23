package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ArangoDB-specific Project entity.
 *
 * Represents the persistence layer for the [Project][com.inso_world.binocular.model.Project] domain object.
 *
 * ### Identity Mapping
 * - [id]: ArangoDB internal document ID (_key)
 * - [iid]: Domain immutable identity (UUID)
 * - [name]: Business key (unique)
 *
 * ### Relationships
 * - [repository]: Optional owning repository (set-once semantics enforced in domain)
 *
 * ### Indexes
 * - [iid]: Unique persistent index for UUID-based lookups
 */
@OptIn(ExperimentalUuidApi::class)
@Document("projects")
data class ProjectEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var name: String,
    var description: String? = null,
) {
    @Ref
    var repository: RepositoryEntity? = null

    /**
     * Converts this ProjectEntity to a Project domain object.
     *
     * @param repo Optional repository to associate with the project
     * @return Project domain object
     */
    fun toDomain(repo: com.inso_world.binocular.model.Repository? = null): com.inso_world.binocular.model.Project {
        return com.inso_world.binocular.model.Project(
            name = this.name
        ).apply {
            this.id = this@ProjectEntity.id
            this.description = this@ProjectEntity.description
            repo?.let { this.repo = it }
        }
    }
}

/**
 * Converts a Project domain object to ProjectEntity.
 *
 * @return ProjectEntity for persistence
 */
@OptIn(ExperimentalUuidApi::class)
internal fun com.inso_world.binocular.model.Project.toEntity(): ProjectEntity = ProjectEntity(
    id = this.id,
    iid = this.iid.value,
    name = this.name,
    description = this.description
)

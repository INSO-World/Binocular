package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    @Ref
    var issues: List<IssueEntity> = emptyList()

    @Ref
    var mergeRequests: List<MergeRequestEntity> = emptyList()

    fun toDomain(repoId: com.inso_world.binocular.model.Repository.Id? = null): com.inso_world.binocular.model.Project {
        return com.inso_world.binocular.model.Project(
            name = this.name
        ).apply {
            this.id = this@ProjectEntity.id
            this.description = this@ProjectEntity.description
            repoId?.let { this.repoId = it }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun com.inso_world.binocular.model.Project.toEntity(): ProjectEntity = ProjectEntity(
    id = this.id,
    iid = this.iid.value,
    name = this.name,
    description = this.description
)

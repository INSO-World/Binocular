package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.arangodb.springframework.annotation.Ref
import com.inso_world.binocular.model.Repository
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Document("repositories")
data class RepositoryEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var localPath: String,
    @Ref(lazy = true)
    val projectId: String
) {
    @Ref
    var commits: List<CommitEntity> = emptyList()

    @Ref
    var files: List<FileEntity> = emptyList()

    fun toDomain(projectId: com.inso_world.binocular.model.Project.Id): com.inso_world.binocular.model.Repository {
        return com.inso_world.binocular.model.Repository(
            localPath = this.localPath.trim(),
            projectId = projectId
        ).apply {
            this.id = this@RepositoryEntity.id
        }
    }
}

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
internal fun com.inso_world.binocular.model.Repository.toEntity(project: ProjectEntity): RepositoryEntity =
    RepositoryEntity(
        id = this.id,
        iid = this.iid.value,
        localPath = this.localPath.trim(),
        projectId = project.id ?: throw IllegalStateException("ProjectEntity must be saved")
    )

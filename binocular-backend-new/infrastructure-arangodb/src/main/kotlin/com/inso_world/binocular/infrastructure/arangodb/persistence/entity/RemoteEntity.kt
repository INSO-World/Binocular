package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Document("remotes")
data class RemoteEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var name: String,
    var url: String,
    var repositoryId: String,
) {
    fun toDomain(): Remote {
        return Remote(
            name = this.name,
            url = this.url,
            repositoryId = Repository.Id(kotlin.uuid.Uuid.parse(this.repositoryId)),
        ).apply {
            this.id = this@RemoteEntity.id
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun Remote.toEntity(repositoryId: String): RemoteEntity =
    RemoteEntity(
        id = this.id,
        iid = this.iid.value,
        name = this.name,
        url = this.url,
        repositoryId = repositoryId,
    )

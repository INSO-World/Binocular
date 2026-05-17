package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.PersistentIndexed
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Revision
import org.springframework.data.annotation.Id
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Document("revisions")
data class RevisionEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    @PersistentIndexed(unique = true)
    var iid: Uuid,
    var content: String? = null,
    var commitId: String,
    var fileId: String,
) {
    fun toDomain(): Revision {
        return Revision(
            content = this.content,
            commitId = Commit.Id(kotlin.uuid.Uuid.parse(this.commitId)),
            fileId = File.Id(kotlin.uuid.Uuid.parse(this.fileId)),
        ).apply {
            this.id = this@RevisionEntity.id
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun Revision.toEntity(): RevisionEntity =
    RevisionEntity(
        id = this.id,
        iid = this.iid.value,
        content = this.content,
        commitId = this.commitId.value.toString(),
        fileId = this.fileId.value.toString(),
    )

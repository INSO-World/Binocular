package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import com.inso_world.binocular.model.Developer
import org.springframework.data.annotation.Id

@Document("developers")
data class DeveloperEntity(
    @Id
    var id: String? = null,
    var gitSignature: String,
    val iid: Developer.Id,
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
    data class Key(val gitSignature: String)

    val name: String
        get() {
            val nameRegex = Regex("""^(.+?)\s*<""")
            return nameRegex.find(gitSignature)?.groupValues?.get(1)?.trim()
                ?: throw IllegalArgumentException("Could not extract name from gitSignature: $gitSignature")
        }

    val email: String
        get() {
            val emailRegex = Regex("""<([^>]+)>$""")
            return emailRegex.find(gitSignature)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("Could not extract email from gitSignature: $gitSignature")
        }

    val uniqueKey: Key
        get() = Key(gitSignature = gitSignature)

    fun toDomain(): Developer =
        Developer(
            name = this.name,
            email = this.email,
        ).apply {
            this.id = this@DeveloperEntity.id
        }
}

internal fun Developer.toEntity(): DeveloperEntity =
    DeveloperEntity(
        id = this.id,
        gitSignature = "${this.name.trim()} ",
        iid = this.iid,
    )

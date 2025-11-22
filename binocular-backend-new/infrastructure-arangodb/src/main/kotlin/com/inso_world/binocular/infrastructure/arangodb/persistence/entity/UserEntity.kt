package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific User entity.
 */
@Document("users")
data class UserEntity(
    @Id
    var id: String? = null,
    var gitSignature: String,
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

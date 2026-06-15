package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Remote entity.
 */
@Document("remotes")
data class RemoteEntity(
    @Id var id: String? = null,
    var name: String,
    var url: String,
    @Ref(lazy = true)
    var repository: RepositoryEntity,
)

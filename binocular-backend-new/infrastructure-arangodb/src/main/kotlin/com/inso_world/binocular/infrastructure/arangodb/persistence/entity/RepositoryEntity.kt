package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id

@Document("repositories")
data class RepositoryEntity(
    @Id
    var id: String? = null,
    var name: String,
    @Ref(lazy = true)
    val project: ProjectEntity
) {
    init {
        this.project.repository = this
    }
}

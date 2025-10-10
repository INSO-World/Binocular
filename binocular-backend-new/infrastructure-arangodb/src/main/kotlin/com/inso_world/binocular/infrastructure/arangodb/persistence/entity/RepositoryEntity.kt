package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import org.springframework.data.annotation.Id

@Document("repositories")
data class RepositoryEntity(
    @Id
    var id: String? = null,
    var name: String,
    // minimal link to project
    var projectId: String? = null,
)

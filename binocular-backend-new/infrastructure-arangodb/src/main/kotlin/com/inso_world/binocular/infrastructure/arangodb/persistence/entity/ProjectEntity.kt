package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Ref
import org.springframework.data.annotation.Id

@Document("projects")
data class ProjectEntity(
    @Id
    var id: String? = null,
    var name: String,
    var description: String? = null,
) {
    @Ref
    var repository: RepositoryEntity? = null
}

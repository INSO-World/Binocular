package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Ref
import com.arangodb.springframework.annotation.Relations
import org.springframework.data.annotation.Id
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.ProjectAccountConnectionEntity

@Document("projects")
data class ProjectEntity(
    @Id
    var id: String? = null,
    var name: String,
    var description: String? = null,
    @Relations(
        edges = [ProjectAccountConnectionEntity::class],
        direction = Relations.Direction.OUTBOUND,
        lazy = true,
        maxDepth = 1
    )
    var accounts: Set<AccountEntity> = emptySet()
) {
    @Ref
    var repository: RepositoryEntity? = null
}

package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : ArangoRepository<ProjectEntity, String> {
    fun findByName(name: String): ProjectEntity?
}

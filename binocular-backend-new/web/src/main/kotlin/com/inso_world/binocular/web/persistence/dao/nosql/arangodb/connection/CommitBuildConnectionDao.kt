package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitBuildConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.BuildEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitBuildConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.BuildMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.BuildRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitBuildConnectionDao.
 * 
 * This class adapts the existing CommitBuildConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class CommitBuildConnectionDao @Autowired constructor(
    private val repository: CommitBuildConnectionRepository,
    private val commitRepository: CommitRepository,
    private val buildRepository: BuildRepository,
    private val commitMapper: CommitMapper,
    private val buildMapper: BuildMapper
) : ICommitBuildConnectionDao {

    /**
     * Find all builds connected to a commit
     */
    override fun findBuildsByCommit(commitId: String): List<Build> {
        val buildEntities = repository.findBuildsByCommit(commitId) as List<Any>
        return buildEntities.map { buildMapper.toDomain(it as BuildEntity) }
    }

    /**
     * Find all commits connected to a build
     */
    override fun findCommitsByBuild(buildId: String): List<Commit> {
        val commitEntities = repository.findCommitsByBuild(buildId) as List<Any>
        return commitEntities.map { commitMapper.toDomain(it as CommitEntity) }
    }

    /**
     * Save a commit-build connection
     */
    override fun save(connection: CommitBuildConnection): CommitBuildConnection {
        // Get the commit and build entities from their repositories
        val commitEntity = commitRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("Commit with ID ${connection.from.id} not found") 
        }
        val buildEntity = buildRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("Build with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the repository entity format
        val entity = CommitBuildConnectionEntity(
            id = connection.id,
            from = commitEntity,
            to = buildEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return CommitBuildConnection(
            id = savedEntity.id,
            from = commitMapper.toDomain(savedEntity.from),
            to = buildMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all commit-build connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}

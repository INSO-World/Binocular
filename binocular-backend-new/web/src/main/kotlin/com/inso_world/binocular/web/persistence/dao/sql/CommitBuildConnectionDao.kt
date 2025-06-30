package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.edge.domain.CommitBuildConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.BuildEntity
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of CommitBuildConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class CommitBuildConnectionDao(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val buildDao: IBuildDao
) : ICommitBuildConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findBuildsByCommit(commitId: String): List<Build> {
        // Use the direct relationship between Commit and Build
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :commitId",
            CommitEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.builds.map { buildDao.findById(it.id!!)!! }
    }

    override fun findCommitsByBuild(buildId: String): List<Commit> {
        // Use the direct relationship between Build and Commit
        val query = entityManager.createQuery(
            "SELECT b FROM BuildEntity b WHERE b.id = :buildId",
            BuildEntity::class.java
        )
        query.setParameter("buildId", buildId)
        val buildEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return buildEntity.commits.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitBuildConnection): CommitBuildConnection {
        val commitId = connection.from.id ?: throw IllegalStateException("Commit ID cannot be null")
        val buildId = connection.to.id ?: throw IllegalStateException("Build ID cannot be null")

        // Find the entities
        val commitEntity = entityManager.find(CommitEntity::class.java, commitId)
            ?: throw IllegalStateException("Commit with ID $commitId not found")
        val buildEntity = entityManager.find(BuildEntity::class.java, buildId)
            ?: throw IllegalStateException("Build with ID $buildId not found")

        // Add the relationship if it doesn't exist
        if (!commitEntity.builds.contains(buildEntity)) {
            commitEntity.builds.add(buildEntity)
            entityManager.merge(commitEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the commit and build
        return CommitBuildConnection(
            id = connectionId,
            from = commitDao.findById(commitId)!!,
            to = buildDao.findById(buildId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between commits and builds
        val commits = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java).resultList
        for (commit in commits) {
            commit.builds.clear()
            entityManager.merge(commit)
        }
    }
}

package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.domain.IssueCommitConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueCommitConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueCommitConnectionMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Profile("sql")
@Transactional
class IssueCommitConnectionDao(
    @Autowired private val issueCommitConnectionMapper: IssueCommitConnectionMapper,
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val commitDao: ICommitDao
) : IIssueCommitConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findCommitsByIssue(issueId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN IssueCommitConnectionEntity ic ON c.id = ic.commitId WHERE ic.issueId = :issueId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun findIssuesByCommit(commitId: String): List<Issue> {
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i JOIN IssueCommitConnectionEntity ic ON i.id = ic.issueId WHERE ic.commitId = :commitId",
            com.inso_world.binocular.web.persistence.entity.sql.IssueEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val issueEntities = query.resultList
        
        // Convert SQL entities to domain models
        return issueEntities.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueCommitConnection): IssueCommitConnection {
        val entity = issueCommitConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM IssueCommitConnectionEntity WHERE issueId = :issueId AND commitId = :commitId",
            IssueCommitConnectionEntity::class.java
        )
            .setParameter("issueId", entity.issueId)
            .setParameter("commitId", entity.commitId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return issueCommitConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return issueCommitConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM IssueCommitConnectionEntity").executeUpdate()
    }
}

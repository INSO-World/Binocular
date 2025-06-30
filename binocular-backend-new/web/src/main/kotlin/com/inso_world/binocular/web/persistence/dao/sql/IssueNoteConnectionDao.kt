package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.IssueNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueNoteConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueNoteConnectionMapper
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
class IssueNoteConnectionDao(
    @Autowired private val issueNoteConnectionMapper: IssueNoteConnectionMapper,
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val noteDao: INoteDao
) : IIssueNoteConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findNotesByIssue(issueId: String): List<Note> {
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n JOIN IssueNoteConnectionEntity inc ON n.id = inc.noteId WHERE inc.issueId = :issueId",
            com.inso_world.binocular.web.persistence.entity.sql.NoteEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val noteEntities = query.resultList
        
        // Convert SQL entities to domain models
        return noteEntities.map { noteDao.findById(it.id!!)!! }
    }

    override fun findIssuesByNote(noteId: String): List<Issue> {
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i JOIN IssueNoteConnectionEntity inc ON i.id = inc.issueId WHERE inc.noteId = :noteId",
            com.inso_world.binocular.web.persistence.entity.sql.IssueEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val issueEntities = query.resultList
        
        // Convert SQL entities to domain models
        return issueEntities.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueNoteConnection): IssueNoteConnection {
        val entity = issueNoteConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM IssueNoteConnectionEntity WHERE issueId = :issueId AND noteId = :noteId",
            IssueNoteConnectionEntity::class.java
        )
            .setParameter("issueId", entity.issueId)
            .setParameter("noteId", entity.noteId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return issueNoteConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return issueNoteConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM IssueNoteConnectionEntity").executeUpdate()
    }
}

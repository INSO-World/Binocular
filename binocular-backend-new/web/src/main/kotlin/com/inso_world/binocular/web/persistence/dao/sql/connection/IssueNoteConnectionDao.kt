package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.IssueNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import com.inso_world.binocular.web.persistence.entity.sql.NoteEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of IssueNoteConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class IssueNoteConnectionDao(
    @Autowired private val issueDao: IIssueDao,
    @Autowired private val noteDao: INoteDao
) : IIssueNoteConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findNotesByIssue(issueId: String): List<Note> {
        // Use the direct relationship between Issue and Note
        val query = entityManager.createQuery(
            "SELECT i FROM IssueEntity i WHERE i.id = :issueId",
            IssueEntity::class.java
        )
        query.setParameter("issueId", issueId)
        val issueEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return issueEntity.notes.map { noteDao.findById(it.id!!)!! }
    }

    override fun findIssuesByNote(noteId: String): List<Issue> {
        // Use the direct relationship between Note and Issue
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n WHERE n.id = :noteId",
            NoteEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val noteEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return noteEntity.issues.map { issueDao.findById(it.id!!)!! }
    }

    override fun save(connection: IssueNoteConnection): IssueNoteConnection {
        val issueId = connection.from.id ?: throw IllegalStateException("Issue ID cannot be null")
        val noteId = connection.to.id ?: throw IllegalStateException("Note ID cannot be null")

        // Find the entities
        val issueEntity = entityManager.find(IssueEntity::class.java, issueId)
            ?: throw IllegalStateException("Issue with ID $issueId not found")
        val noteEntity = entityManager.find(NoteEntity::class.java, noteId)
            ?: throw IllegalStateException("Note with ID $noteId not found")

        // Add the relationship if it doesn't exist
        if (!issueEntity.notes.contains(noteEntity)) {
            issueEntity.notes.add(noteEntity)
            entityManager.merge(issueEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the issue and note
        return IssueNoteConnection(
            id = connectionId,
            from = issueDao.findById(issueId)!!,
            to = noteDao.findById(noteId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between issues and notes
        val issues = entityManager.createQuery("SELECT i FROM IssueEntity i", IssueEntity::class.java)
            .resultList
        issues.forEach { issue ->
            issue.notes.clear()
            entityManager.merge(issue)
        }
    }
}

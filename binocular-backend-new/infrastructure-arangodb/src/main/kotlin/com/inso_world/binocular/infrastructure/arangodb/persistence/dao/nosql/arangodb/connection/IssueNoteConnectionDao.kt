package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueNoteConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueNoteConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueNoteConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.NoteMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.IssueRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.NoteRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueNoteConnectionDao.
 *
 * This class adapts the existing IssueNoteConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class IssueNoteConnectionDao
    @Autowired
    constructor(
        private val repository: IssueNoteConnectionRepository,
        private val issueRepository: IssueRepository,
        private val noteRepository: NoteRepository,
        private val issueMapper: IssueMapper,
        private val noteMapper: NoteMapper,
    ) : IIssueNoteConnectionDao {
        /**
         * Find all notes connected to an issue
         */
        override fun findNotesByIssue(issueId: String): List<Note> {
            val noteEntities = repository.findNotesByIssue(issueId)
            return noteEntities.map { noteMapper.toDomain(it) }
        }

        /**
         * Find all issues connected to a note
         */
        override fun findIssuesByNote(noteId: String): List<Issue> {
            val issueEntities = repository.findIssuesByNote(noteId)
            return issueEntities.map { issueMapper.toDomain(it) }
        }

        /**
         * Save an issue-note connection
         */
        override fun save(connection: IssueNoteConnection): IssueNoteConnection {
            // Get the issue and note entities from their repositories
            val issueEntity =
                issueRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Issue with ID ${connection.from.id} not found")
                }
            val noteEntity =
                noteRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Note with ID ${connection.to.id} not found")
                }

            // Convert domain model to the entity format
            val entity =
                IssueNoteConnectionEntity(
                    id = connection.id,
                    from = issueEntity,
                    to = noteEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return IssueNoteConnection(
                id = savedEntity.id,
                from = issueMapper.toDomain(savedEntity.from),
                to = noteMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all issue-note connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }

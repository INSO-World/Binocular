package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.NoteAccountConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.INoteAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.NoteAccountConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.NoteMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.NoteRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.NoteAccountConnectionRepository
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of INoteAccountConnectionDao.
 *
 * This class adapts the existing NoteAccountConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class NoteAccountConnectionDao
    @Autowired
    constructor(
        private val repository: NoteAccountConnectionRepository,
        private val noteRepository: NoteRepository,
        private val accountRepository: AccountRepository,
        private val noteMapper: NoteMapper,
        private val accountMapper: AccountMapper,
    ) : INoteAccountConnectionDao {
        /**
         * Find all accounts connected to a note
         */
        override fun findAccountsByNote(noteId: String): List<Account> {
            val accountEntities = repository.findAccountsByNote(noteId)
            return accountEntities.map { accountMapper.toDomain(it) }
        }

        /**
         * Find all notes connected to an account
         */
        override fun findNotesByAccount(accountId: String): List<Note> {
            val noteEntities = repository.findNotesByAccount(accountId)
            return noteEntities.map { noteMapper.toDomain(it) }
        }

        /**
         * Save a note-account connection
         */
        override fun save(connection: NoteAccountConnection): NoteAccountConnection {
            // Get the note and account entities from their repositories
            val noteEntity =
                noteRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("Note with ID ${connection.from.id} not found")
                }
            val accountEntity =
                accountRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Account with ID ${connection.to.id} not found")
                }

            // Convert domain model to the entity format
            val entity =
                NoteAccountConnectionEntity(
                    id = connection.id,
                    from = noteEntity,
                    to = accountEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return NoteAccountConnection(
                id = savedEntity.id,
                from = noteMapper.toDomain(savedEntity.from),
                to = accountMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all note-account connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }

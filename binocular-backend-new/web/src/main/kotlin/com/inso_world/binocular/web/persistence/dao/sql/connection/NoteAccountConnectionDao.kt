package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.NoteAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.AccountEntity
import com.inso_world.binocular.web.persistence.entity.sql.NoteEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of NoteAccountConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class NoteAccountConnectionDao(
    @Autowired private val noteDao: INoteDao,
    @Autowired private val accountDao: IAccountDao
) : INoteAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByNote(noteId: String): List<Account> {
        // Use the direct relationship between Note and Account
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n WHERE n.id = :noteId",
            NoteEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val noteEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return noteEntity.accounts.map { accountDao.findById(it.id!!)!! }
    }

    override fun findNotesByAccount(accountId: String): List<Note> {
        // Use the direct relationship between Account and Note
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a WHERE a.id = :accountId",
            AccountEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val accountEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return accountEntity.notes.map { noteDao.findById(it.id!!)!! }
    }

    override fun save(connection: NoteAccountConnection): NoteAccountConnection {
        val noteId = connection.from.id ?: throw IllegalStateException("Note ID cannot be null")
        val accountId = connection.to.id ?: throw IllegalStateException("Account ID cannot be null")

        // Find the entities
        val noteEntity = entityManager.find(NoteEntity::class.java, noteId)
            ?: throw IllegalStateException("Note with ID $noteId not found")
        val accountEntity = entityManager.find(AccountEntity::class.java, accountId)
            ?: throw IllegalStateException("Account with ID $accountId not found")

        // Add the relationship if it doesn't exist
        if (!accountEntity.notes.contains(noteEntity)) {
            accountEntity.notes.add(noteEntity)
            entityManager.merge(accountEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the note and account
        return NoteAccountConnection(
            id = connectionId,
            from = noteDao.findById(noteId)!!,
            to = accountDao.findById(accountId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between accounts and notes
        val accounts = entityManager.createQuery("SELECT a FROM AccountEntity a", AccountEntity::class.java)
            .resultList
        accounts.forEach { account ->
            account.notes.clear()
            entityManager.merge(account)
        }
    }
}

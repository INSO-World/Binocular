package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.NoteAccountConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.NoteAccountConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.NoteAccountConnectionMapper
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
class NoteAccountConnectionDao(
    @Autowired private val noteAccountConnectionMapper: NoteAccountConnectionMapper,
    @Autowired private val noteDao: INoteDao,
    @Autowired private val accountDao: IAccountDao
) : INoteAccountConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findAccountsByNote(noteId: String): List<Account> {
        val query = entityManager.createQuery(
            "SELECT a FROM AccountEntity a JOIN NoteAccountConnectionEntity c ON a.id = c.accountId WHERE c.noteId = :noteId",
            com.inso_world.binocular.web.persistence.entity.sql.AccountEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val accountEntities = query.resultList
        
        // Convert SQL entities to domain models
        return accountEntities.map { accountDao.findById(it.id!!)!! }
    }

    override fun findNotesByAccount(accountId: String): List<Note> {
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n JOIN NoteAccountConnectionEntity c ON n.id = c.noteId WHERE c.accountId = :accountId",
            com.inso_world.binocular.web.persistence.entity.sql.NoteEntity::class.java
        )
        query.setParameter("accountId", accountId)
        val noteEntities = query.resultList
        
        // Convert SQL entities to domain models
        return noteEntities.map { noteDao.findById(it.id!!)!! }
    }

    override fun save(connection: NoteAccountConnection): NoteAccountConnection {
        val entity = noteAccountConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM NoteAccountConnectionEntity WHERE noteId = :noteId AND accountId = :accountId",
            NoteAccountConnectionEntity::class.java
        )
            .setParameter("noteId", entity.noteId)
            .setParameter("accountId", entity.accountId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return noteAccountConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return noteAccountConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM NoteAccountConnectionEntity").executeUpdate()
    }
}

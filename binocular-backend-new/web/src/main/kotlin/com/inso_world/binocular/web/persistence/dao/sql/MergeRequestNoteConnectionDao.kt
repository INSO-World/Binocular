package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.domain.MergeRequestNoteConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestNoteConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.MergeRequestNoteConnectionMapper
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
class MergeRequestNoteConnectionDao(
    @Autowired private val mergeRequestNoteConnectionMapper: MergeRequestNoteConnectionMapper,
    @Autowired private val mergeRequestDao: IMergeRequestDao,
    @Autowired private val noteDao: INoteDao
) : IMergeRequestNoteConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findNotesByMergeRequest(mergeRequestId: String): List<Note> {
        val query = entityManager.createQuery(
            "SELECT n FROM NoteEntity n JOIN MergeRequestNoteConnectionEntity c ON n.id = c.noteId WHERE c.mergeRequestId = :mergeRequestId",
            com.inso_world.binocular.web.persistence.entity.sql.NoteEntity::class.java
        )
        query.setParameter("mergeRequestId", mergeRequestId)
        val noteEntities = query.resultList
        
        // Convert SQL entities to domain models
        return noteEntities.map { noteDao.findById(it.id!!)!! }
    }

    override fun findMergeRequestsByNote(noteId: String): List<MergeRequest> {
        val query = entityManager.createQuery(
            "SELECT mr FROM MergeRequestEntity mr JOIN MergeRequestNoteConnectionEntity c ON mr.id = c.mergeRequestId WHERE c.noteId = :noteId",
            com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity::class.java
        )
        query.setParameter("noteId", noteId)
        val mergeRequestEntities = query.resultList
        
        // Convert SQL entities to domain models
        return mergeRequestEntities.map { mergeRequestDao.findById(it.id!!)!! }
    }

    override fun save(connection: MergeRequestNoteConnection): MergeRequestNoteConnection {
        val entity = mergeRequestNoteConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM MergeRequestNoteConnectionEntity WHERE mergeRequestId = :mergeRequestId AND noteId = :noteId",
            MergeRequestNoteConnectionEntity::class.java
        )
            .setParameter("mergeRequestId", entity.mergeRequestId)
            .setParameter("noteId", entity.noteId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return mergeRequestNoteConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return mergeRequestNoteConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM MergeRequestNoteConnectionEntity").executeUpdate()
    }
}

package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.model.Note
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
internal class NoteDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    fun findById(id: String): Note? =
        try {
            em.createNativeQuery("select id, body, created_at, updated_at, system, resolvable, confidential, internal, imported, imported_from from notes where id = ?1")
                .setParameter(1, id)
                .resultList
                .firstOrNull()
                ?.let { row ->
                    val arr = row as Array<*>
                    mapRow(arr)
                }
        } catch (ex: Exception) {
            null
        }

    fun findAll(): List<Note> =
        em.createNativeQuery("select id, body, created_at, updated_at, system, resolvable, confidential, internal, imported, imported_from from notes order by id")
            .resultList
            .map { row -> mapRow(row as Array<*>) }

    fun count(): Long =
        (em.createNativeQuery("select count(*) from notes").singleResult as Number).toLong()

    fun findAll(pageable: Pageable): List<Note> =
        em.createNativeQuery("select id, body, created_at, updated_at, system, resolvable, confidential, internal, imported, imported_from from notes order by id limit ?1 offset ?2")
            .setParameter(1, pageable.pageSize)
            .setParameter(2, pageable.offset.toInt())
            .resultList
            .map { row -> mapRow(row as Array<*>) }

    fun create(note: Note): Note {
        em.createNativeQuery("insert into notes (id, body, created_at, updated_at, system, resolvable, confidential, internal, imported, imported_from) values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)")
            .setParameter(1, note.id)
            .setParameter(2, note.body)
            .setParameter(3, note.createdAt)
            .setParameter(4, note.updatedAt)
            .setParameter(5, note.system)
            .setParameter(6, note.resolvable)
            .setParameter(7, note.confidential)
            .setParameter(8, note.internal)
            .setParameter(9, note.imported)
            .setParameter(10, note.importedFrom)
            .executeUpdate()
        return note
    }

    fun update(note: Note): Note {
        em.createNativeQuery("update notes set body = ?1, created_at = ?2, updated_at = ?3, system = ?4, resolvable = ?5, confidential = ?6, internal = ?7, imported = ?8, imported_from = ?9 where id = ?10")
            .setParameter(1, note.body)
            .setParameter(2, note.createdAt)
            .setParameter(3, note.updatedAt)
            .setParameter(4, note.system)
            .setParameter(5, note.resolvable)
            .setParameter(6, note.confidential)
            .setParameter(7, note.internal)
            .setParameter(8, note.imported)
            .setParameter(9, note.importedFrom)
            .setParameter(10, note.id)
            .executeUpdate()
        return note
    }

    fun deleteById(id: String) {
        em.createNativeQuery("delete from notes where id = ?1")
            .setParameter(1, id)
            .executeUpdate()
    }

    fun deleteAll() {
        em.createNativeQuery("delete from notes").executeUpdate()
    }

    private fun mapRow(arr: Array<*>): Note =
        Note(
            id = arr[0]?.toString(),
            body = arr[1]?.toString() ?: "",
            createdAt = arr[2]?.toString() ?: "",
            updatedAt = arr[3]?.toString() ?: "",
            system = (arr[4] as? Boolean) ?: false,
            resolvable = (arr[5] as? Boolean) ?: false,
            confidential = (arr[6] as? Boolean) ?: false,
            internal = (arr[7] as? Boolean) ?: false,
            imported = (arr[8] as? Boolean) ?: false,
            importedFrom = arr[9]?.toString() ?: ""
        )
}

package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDao
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.util.stream.Stream

@Repository
internal class SqlDao<T, I : Serializable> : IDao<T, I> {
    private lateinit var clazz: Class<T>
    private lateinit var repository: JpaRepository<T, I>

    @PersistenceContext
    protected lateinit var entityManager: EntityManager

    fun setRepository(repo: JpaRepository<T, I>) {
        this.repository = repo
    }

    fun setClazz(clazz: Class<T>) {
        this.clazz = clazz
    }

    override fun findById(id: I): T? = entityManager.find(clazz, id)

    override fun findAll(): List<T> = entityManager.createQuery("FROM ${clazz.name}", clazz).resultList

    override fun findAll(pageable: Pageable): Page<T> {
        val query = entityManager.createQuery("FROM ${clazz.name}", clazz)
        val totalElements =
            entityManager.createQuery("SELECT COUNT(e) FROM ${clazz.name} e", Long::class.java).singleResult

        val content =
            query
                .setFirstResult(pageable.pageNumber * pageable.pageSize)
                .setMaxResults(pageable.pageSize)
                .resultList

        return Page(content, totalElements, pageable)
    }

    override fun findAllAsStream(): Stream<T> = entityManager.createQuery("SELECT e FROM ${clazz.name} e", clazz).resultStream

    override fun create(entity: T): T {
        entityManager.persist(entity)
        return entity
    }

    override fun update(entity: T): T = entityManager.merge(entity)

    override fun updateAndFlush(entity: T): T {
        val updated = entityManager.merge(entity)
        entityManager.flush()
        return updated
    }

    override fun delete(entity: T) {
        entityManager.remove(entity)
    }

    override fun deleteById(id: I) {
        val entity = findById(id) ?: throw NotFoundException("Entity not found")
        delete(entity)
    }

    /**
     * Delete all entities
     */
    override fun deleteAll() {
//        val entities =
//            entityManager
//                .createQuery("SELECT e FROM ${clazz.name} e", clazz)
//                .resultList
//        entities.forEach { delete(it) }
        this.repository.deleteAll()
//        entityManager.createQuery("DELETE FROM ${clazz.name}").executeUpdate()
    }

    /**
     * Save an entity (create or update)
     * For SQL, this is the same as create or update depending on whether the entity exists
     */
    @Deprecated("to be deleted")
    override fun save(entity: T): T =
        if (entityManager.contains(entity)) {
            update(entity)
        } else {
            create(entity)
        }

    /**
     * Save multiple entities
     */
    override fun saveAll(entities: Collection<T>): Iterable<T> = this.repository.saveAll(entities)

    override fun flush() {
        entityManager.flush()
    }
}

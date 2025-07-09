// package com.inso_world.binocular.infrastructure.sql.persistence.dao
//
// import com.inso_world.binocular.core.persistence.dao.interfaces.IFileDao
// import com.inso_world.binocular.core.persistence.model.Page
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.FileEntity
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.FileMapper
// import com.inso_world.binocular.model.File
// import jakarta.persistence.EntityManager
// import jakarta.persistence.PersistenceContext
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Profile
// import org.springframework.data.domain.Pageable
// import org.springframework.stereotype.Repository
// import org.springframework.transaction.annotation.Transactional
//
// /**
// * SQL implementation of IFileDao.
// */
// @Repository
// @Transactional
// class FileDao(
//    @Autowired private val fileMapper: FileMapper,
// ) : IFileDao {
//    @PersistenceContext
//    private lateinit var entityManager: EntityManager
//
//    override fun findById(id: String): File? {
//        val entity = entityManager.find(FileEntity::class.java, id) ?: return null
//        return fileMapper.toDomain(entity)
//    }
//
//    override fun findAll(): Iterable<File> {
//        val query = entityManager.createQuery("FROM FileEntity", FileEntity::class.java)
//        val entities = query.resultList
//        return fileMapper.toDomainList(entities)
//    }
//
//    override fun findAll(pageable: Pageable): Page<File> {
//        val query = entityManager.createQuery("FROM FileEntity", FileEntity::class.java)
//        val countQuery = entityManager.createQuery("SELECT COUNT(f) FROM FileEntity f", Long::class.java)
//        val totalElements = countQuery.singleResult
//
//        val entities =
//            query
//                .setFirstResult(pageable.pageNumber * pageable.pageSize)
//                .setMaxResults(pageable.pageSize)
//                .resultList
//
//        val content = fileMapper.toDomainList(entities)
//        return Page(content, totalElements, pageable)
//    }
//
//    override fun create(entity: File): File {
//        val fileEntity = fileMapper.toEntity(entity)
//        entityManager.persist(fileEntity)
//        return fileMapper.toDomain(fileEntity)
//    }
//
//    override fun update(entity: File): File {
//        val fileEntity = fileMapper.toEntity(entity)
//        val mergedEntity = entityManager.merge(fileEntity)
//        return fileMapper.toDomain(mergedEntity)
//    }
//
//    override fun updateAndFlush(entity: File): File {
//        val result = update(entity)
//        entityManager.flush()
//        return result
//    }
//
//    override fun delete(entity: File) {
//        val fileEntity = fileMapper.toEntity(entity)
//        val managedEntity = entityManager.merge(fileEntity)
//        entityManager.remove(managedEntity)
//    }
//
//    override fun deleteById(id: String) {
//        val entity = entityManager.find(FileEntity::class.java, id) ?: return
//        entityManager.remove(entity)
//    }
//
//    /**
//     * Delete all entities
//     */
//    override fun deleteAll() {
//        val files =
//            entityManager
//                .createQuery("SELECT f FROM FileEntity f", FileEntity::class.java)
//                .resultList
//        files.forEach { entityManager.remove(it) }
//    }
//
//    /**
//     * Save an entity (create or update)
//     * For SQL, this is the same as create or update depending on whether the entity exists
//     */
//    override fun save(entity: File): File {
//        val fileEntity = fileMapper.toEntity(entity)
//        return if (entityManager.find(FileEntity::class.java, fileEntity.id) != null) {
//            update(entity)
//        } else {
//            create(entity)
//        }
//    }
//
//    /**
//     * Save multiple entities
//     */
//    override fun saveAll(entities: List<File>): Iterable<File> = entities.map { save(it) }
// }

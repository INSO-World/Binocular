// package com.inso_world.binocular.infrastructure.sql.persistence.dao
//
// import com.inso_world.binocular.core.persistence.dao.interfaces.IBuildDao
// import com.inso_world.binocular.core.persistence.model.Page
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.BuildEntity
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.BuildMapper
// import com.inso_world.binocular.model.Build
// import jakarta.persistence.EntityManager
// import jakarta.persistence.PersistenceContext
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Profile
// import org.springframework.data.domain.Pageable
// import org.springframework.stereotype.Repository
// import org.springframework.transaction.annotation.Transactional
// import kotlin.jvm.java
//
// /**
// * SQL implementation of IBuildDao.
// */
// @Repository
// @Transactional
// class BuildDao(
//    @Autowired private val buildMapper: BuildMapper,
// ) : IBuildDao {
//    @PersistenceContext
//    private lateinit var entityManager: EntityManager
//
//    override fun findById(id: String): Build? {
//        val entity = entityManager.find(BuildEntity::class.java, id) ?: return null
//        return buildMapper.toDomain(entity)
//    }
//
//    override fun findAll(): Iterable<Build> {
//        val query = entityManager.createQuery("FROM BuildEntity", BuildEntity::class.java)
//        val entities = query.resultList
//        return buildMapper.toDomainList(entities)
//    }
//
//    override fun findAll(pageable: Pageable): Page<Build> {
//        val query = entityManager.createQuery("FROM BuildEntity", BuildEntity::class.java)
//        val countQuery = entityManager.createQuery("SELECT COUNT(b) FROM BuildEntity b", Long::class.java)
//        val totalElements = countQuery.singleResult
//
//        val entities =
//            query
//                .setFirstResult(pageable.pageNumber * pageable.pageSize)
//                .setMaxResults(pageable.pageSize)
//                .resultList
//
//        val content = buildMapper.toDomainList(entities)
//        return Page(content, totalElements, pageable)
//    }
//
//    override fun create(entity: Build): Build {
//        val buildEntity = buildMapper.toEntity(entity)
//        entityManager.persist(buildEntity)
//        return buildMapper.toDomain(buildEntity)
//    }
//
//    override fun update(entity: Build): Build {
//        val buildEntity = buildMapper.toEntity(entity)
//        val mergedEntity = entityManager.merge(buildEntity)
//        return buildMapper.toDomain(mergedEntity)
//    }
//
//    override fun updateAndFlush(entity: Build): Build {
//        val result = update(entity)
//        entityManager.flush()
//        return result
//    }
//
//    override fun delete(entity: Build) {
//        val buildEntity = buildMapper.toEntity(entity)
//        val managedEntity = entityManager.merge(buildEntity)
//        entityManager.remove(managedEntity)
//    }
//
//    override fun deleteById(id: String) {
//        val entity = entityManager.find(BuildEntity::class.java, id) ?: return
//        entityManager.remove(entity)
//    }
//
//    /**
//     * Delete all entities
//     */
//    override fun deleteAll() {
//        entityManager
//            .createQuery("SELECT b FROM BuildEntity b", BuildEntity::class.java)
//            .resultList
//            .forEach { entityManager.remove(it) }
//    }
//
//    /**
//     * Save an entity (create or update)
//     * For SQL, this is the same as create or update depending on whether the entity exists
//     */
//    override fun save(entity: Build): Build {
//        val buildEntity = buildMapper.toEntity(entity)
//        return if (entityManager.find(BuildEntity::class.java, buildEntity.id) != null) {
//            update(entity)
//        } else {
//            create(entity)
//        }
//    }
//
//    /**
//     * Save multiple entities
//     */
//    override fun saveAll(entities: List<Build>): Iterable<Build> = entities.map { save(it) }
// }

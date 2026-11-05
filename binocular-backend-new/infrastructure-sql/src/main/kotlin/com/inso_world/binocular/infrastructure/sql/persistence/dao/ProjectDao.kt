package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.ProjectRepository
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi

@Repository
internal class ProjectDao(
    @Autowired private val repo: ProjectRepository,
    @Autowired @Lazy private val repositoryDao: RepositoryDao,
) : SqlDao<ProjectEntity, Long>(),
    IProjectDao {
    init {
        this.setClazz(ProjectEntity::class.java)
        this.setRepository(repo)
    }

    override fun findByName(name: String): ProjectEntity? = repo.findByName(name)

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(iid: Project.Id): ProjectEntity? = repo.findByIid(iid.value)

//    @Transactional
//    override fun delete(entity: ProjectEntity) {
//        val toDelete =
//            getManagedEntity(entity) ?: throw IllegalArgumentException("ProjectEntity not found")
//        super.delete(toDelete)
//    }

//    private fun getManagedEntity(entity: ProjectEntity): ProjectEntity? {
//        val managed =
//            entity.id
//                ?.let {
//                    entityManager.find(ProjectEntity::class.java, it)
//                }?.let {
//                    findByName(it.name)
//                }
//
// //        managed?.repo = managed?.repo?.let { repo ->
// //            entityManager.find(Repo)
// //        }
//
//        return managed
//    }

//    @Transactional
//    override fun update(entity: ProjectEntity): ProjectEntity {
//        val toUpdate =
//            getManagedEntity(entity) ?: throw IllegalArgumentException("ProjectEntity not found")
//
//        toUpdate.repo = entity.repo?.let { repositoryDao.getManagedEntity(it) }
//
//        return super.update(toUpdate)
//    }

//    override fun create(entity: ProjectEntity): ProjectEntity = super.create(entity)
}

package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.RepositoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class RepositoryDao(
    @Autowired
    private val repo: RepositoryRepository,
) : SqlDao<RepositoryEntity, Long>(),
    IRepositoryDao {
    init {
        this.setClazz(RepositoryEntity::class.java)
        this.setRepository(repo)
    }

    override fun findByName(name: String): RepositoryEntity? = repo.findByName(name)
//    override fun delete(entity: RepositoryEntity) {
//        val toDelete = getManagedEntity(entity) ?: return
//
//        // Ensure the project is managed by JPA, TODO can be omitted?
//        toDelete.project.id?.let { id ->
//            //            val managedProject =
//            Optional
//                .ofNullable(
//                    entityManager.find(
//                        toDelete.project.javaClass,
//                        id,
//                    ),
//                ).getOrNull()
//                ?.let { managedProject ->
//                    toDelete.project = managedProject
//                }
//        }
//
//        super.delete(toDelete)
//    }

//    override fun create(entity: RepositoryEntity): RepositoryEntity {
//        val toSave =
//            entity.id
//                ?.let {
//                    entityManager.find(RepositoryEntity::class.java, it)
//                } ?: findByName(entity.name)
//        return toSave?.let {
//            super.create(entity)
//        } ?: throw IllegalArgumentException("RepositoryEntity not found")
//    }

//    override fun update(entity: RepositoryEntity): RepositoryEntity {
//        val toUpdate =
//            getManagedEntity(entity) ?: throw IllegalArgumentException("RepositoryEntity not found, cannot update non existing entity")
//
//        toUpdate.project = entity.project.id?.let {
//            entityManager.find(ProjectEntity::class.java, it)
//        } ?: toUpdate.project
//
// //        TODO merge not overwrite
//        val cmts =
//            entity.commits
//                .map { commit ->
//                    val managedCmt = commitDao.getManagedEntity(commit) ?: commit
//                    managedCmt.parents =
//                        commit.parents.map { parent ->
//                            commitDao.getManagedEntity(parent) ?: parent
//                        }
//                    managedCmt
//                }
//        toUpdate.commits.addAll(cmts)
//
//        val branches =
//            entity.branches.map { branch ->
//                branchDao.getManagedEntity(branch) ?: branch
//            }
//        branches.forEach { branch ->
//            branch.commits =
//                branch.commits
//                    .map { commit ->
//                        val managedCmt = commitDao.getManagedEntity(commit) ?: commit
//                        managedCmt.parents =
//                            commit.parents.map { parent ->
//                                val managedPrnt = commitDao.getManagedEntity(parent) ?: parent
//                                managedPrnt.branches.add(branch)
//                                managedPrnt
//                            }
//                        managedCmt.branches.add(branch)
//                        managedCmt
//                    }.toMutableSet()
//        }
//        toUpdate.branches.addAll(branches)
//
//        return super.update(toUpdate)
//    }
//
//    //    TODO proper managed entity, missing commits here
//    fun getManagedEntity(entity: RepositoryEntity): RepositoryEntity? {
//        val managed =
//            entity.id
//                ?.let {
//                    entityManager.find(RepositoryEntity::class.java, it)
//                } ?: findByName(entity.name)
//
//        return managed ?: entity
//    }

//    override fun updateAndFlush(entity: RepositoryEntity): RepositoryEntity {
//        val toUpdate =
//            getManagedEntity(entity) ?: throw IllegalArgumentException("RepositoryEntity not found")
//
// //        TODO refactor
// //        entityManager.flush()
//        return super.updateAndFlush(toUpdate)
//    }
}

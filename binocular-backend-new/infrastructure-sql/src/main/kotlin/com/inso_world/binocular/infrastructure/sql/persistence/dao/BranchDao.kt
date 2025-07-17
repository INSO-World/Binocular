package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.BranchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * SQL implementation of IBranchDao.
 */
@Repository
internal class BranchDao(
    @Autowired private val repo: BranchRepository,
) : SqlDao<BranchEntity, Long>(),
    IBranchDao {
    init {
        this.setClazz(BranchEntity::class.java)
        this.setRepository(repo)
    }

    fun getManagedEntity(entity: BranchEntity): BranchEntity? {
        val managed =
            entity.id?.let {
                entityManager.find(BranchEntity::class.java, it)
            } ?: entity.repository?.let { repo ->
                findByName(repo, entity.name)
            }

        return managed ?: entity
    }

    fun findByName(
        repo: RepositoryEntity,
        name: String,
    ): BranchEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndName(repo.id, name)
    }

    fun findAllByRepository(repo: RepositoryEntity): Collection<BranchEntity> = this.repo.findAllByRepository(repo)
}

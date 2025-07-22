package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.BranchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
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

    private object BranchSpecification {
        fun hasRepository(repository: com.inso_world.binocular.model.Repository): Specification<BranchEntity> =
            Specification { root, query, cb ->
                cb.equal(
                    root.get<RepositoryEntity>("repository").get<String>("name"),
                    repository.name,
                )
            }
    }

    fun findByName(
        repo: RepositoryEntity,
        name: String,
    ): BranchEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndName(repo.id, name)
    }

    override fun findAll(repository: com.inso_world.binocular.model.Repository): Iterable<BranchEntity> =
        this.repo.findAll(
            Specification.allOf(BranchSpecification.hasRepository(repository)),
        )
}

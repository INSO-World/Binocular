package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.BranchRepository
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Reference
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi

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
                    root.get<RepositoryEntity>("repository").get<String>("local_path"),
                    repository.localPath,
                )
            }
    }

    fun findByName(
        repo: RepositoryEntity,
        name: String,
    ): BranchEntity? {
        val rId = repo.id ?: throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndName(rId, name)
    }

    override fun findAll(repository: com.inso_world.binocular.model.Repository): Iterable<BranchEntity> =
        this.repo.findAll(
            Specification.allOf(BranchSpecification.hasRepository(repository)),
        )

    override fun findByIid(iid: Reference.Id): BranchEntity? {
        @OptIn(ExperimentalUuidApi::class)
        return this.repo.findByIid(iid.value)
    }
}

package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.RepositoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
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

    override fun findByIdWithAllRelations(id: Long): RepositoryEntity? = repo.findByIdWithAllRelations(id)

    override fun findByName(name: String): RepositoryEntity? = repo.findByName(name)

    private object RepositorySpecification {
        fun <T> hasRepository(name: String): Specification<T> =
            Specification { root, query, cb ->
                cb.equal(
                    root.get<RepositoryEntity>("repository").get<String>("name"),
                    name,
                )
            }
    }
}

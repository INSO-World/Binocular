package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.RepositoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

@Repository
@Validated
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
}

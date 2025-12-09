package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.RepositoryRepository
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi

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

    override fun findByName(name: String): RepositoryEntity? = repo.findByLocalPath(name)

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(iid: com.inso_world.binocular.model.Repository.Id): RepositoryEntity? = this.repo.findByIid(iid.value)
}

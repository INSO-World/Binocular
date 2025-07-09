package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IRepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
internal class RepositoryInfrastructurePortImpl(
    @Autowired private val projectMapper: RepositoryMapper,
) : AbstractInfrastructurePort<Repository, RepositoryEntity, Long>(Long::class),
    RepositoryInfrastructurePort {
    @Autowired
    private lateinit var projectDao: IRepositoryDao

    @PostConstruct
    fun init() {
        super.dao = projectDao
        super.mapper = projectMapper
    }

    override fun findByName(name: String): Repository? =
        this.projectDao.findByName(name)?.let {
            this.projectMapper.toDomain(it)
        }
}

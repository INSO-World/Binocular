package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

@Repository
@Validated
internal class ProjectDao(
    @Autowired
    private val repo: ProjectRepository,
) : SqlDao<ProjectEntity, Long>(),
    IProjectDao {
    init {
        this.setClazz(ProjectEntity::class.java)
        this.setRepository(repo)
    }

    override fun findByName(name: String): ProjectEntity? = repo.findByName(name)
}

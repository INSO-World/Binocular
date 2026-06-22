package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toSqlEntity
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper : EntityMapper<Project, ProjectEntity> {
    companion object {
        val logger by logger()
    }

    override fun toEntity(domain: Project): ProjectEntity {
        val entity = domain.toSqlEntity()
        return entity
    }

    override fun toDomain(entity: ProjectEntity): Project {
        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        return domain
    }

    fun refreshDomain(target: Project, entity: ProjectEntity) : Project {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )
        return target
    }
}

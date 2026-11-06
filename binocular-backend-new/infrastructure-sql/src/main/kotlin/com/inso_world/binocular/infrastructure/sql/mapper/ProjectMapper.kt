package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper : EntityMapper<Project, ProjectEntity> {
    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var ctx: MappingContext

    override fun toEntity(domain: Project): ProjectEntity {
        ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)?.let { return it }

        val entity = domain.toEntity()

        ctx.remember(domain, entity)

        return entity
    }


    override fun toDomain(entity: ProjectEntity): Project {
        ctx.findDomain<Project, ProjectEntity>(entity)?.let { return it }

        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)

        return domain
    }

    fun refreshDomain(target: Project, entity: ProjectEntity) {
        target.id = entity.id?.toString()
    }
}

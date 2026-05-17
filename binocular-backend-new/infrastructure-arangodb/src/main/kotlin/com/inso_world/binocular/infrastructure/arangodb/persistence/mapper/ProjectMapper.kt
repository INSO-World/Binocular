package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper : EntityMapper<Project, ProjectEntity> {

    @Autowired
    private lateinit var issueMapper: IssueMapper

    @Autowired
    private lateinit var mergeRequestMapper: MergeRequestMapper

    override fun toEntity(domain: Project): ProjectEntity {
        return domain.toEntity().apply {
            issues = domain.issues.map { issueMapper.toEntity(it) }
            mergeRequests = domain.mergeRequests.map { mergeRequestMapper.toEntity(it) }
        }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun toDomain(entity: ProjectEntity): Project {
        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid,
        )
        return domain
    }

    fun refreshDomain(target: Project, entity: ProjectEntity) {
        target.id = entity.id
    }
}

package com.inso_world.binocular.infrastructure.arangodb.assembler

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class RepositoryAssembler {
    companion object {
        private val logger by logger()
    }

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var commitMapper: CommitMapper

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    @Autowired
    @Lazy
    private lateinit var developerMapper: DeveloperMapper

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    private lateinit var ctx: MappingContext

    fun toEntity(domain: Repository): RepositoryEntity {
        logger.debug("Assembling RepositoryEntity for repository: ${domain.localPath}")

        ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)?.let {
            logger.trace("Repository already in context, returning cached entity")
            return it
        }

        val projectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(Project(name = "temp"))
            ?: run {
                logger.trace("Project not in context, mapping minimal Project structure")
                val proj = Project(name = "unknown")
                projectMapper.toEntity(proj)
            }

        logger.trace("Project reference in context: id=${projectEntity.id}")

        val entity = repositoryMapper.toEntity(domain)
        logger.trace("Mapped Repository structure: id=${entity.id}")

        logger.debug(
            "Assembled RepositoryEntity: id=${entity.id}, " +
                    "commits=${domain.commitIds.size}, branches=${domain.branchIds.size}"
        )

        return entity
    }

    fun toDomain(entity: RepositoryEntity): Repository {
        logger.debug("Assembling Repository domain for entity id=${entity.id}")

        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let {
            logger.trace("Repository already in context, returning cached domain")
            return it
        }

        val domain = repositoryMapper.toDomain(entity)
        logger.trace("Mapped Repository structure: ${domain.localPath}")

        logger.debug("Assembled Repository domain: ${domain.localPath}")

        return domain
    }
}
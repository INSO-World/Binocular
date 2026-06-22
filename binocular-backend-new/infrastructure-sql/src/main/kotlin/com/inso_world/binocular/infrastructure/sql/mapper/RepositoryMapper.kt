package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toSqlEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Repository aggregate root.
 *
 * Converts between Repository domain objects and RepositoryEntity persistence entities.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * child entity mapping. Use [RepositoryAssembler] for complete aggregate assembly.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Repository structure (not children)
 * - **Aggregate Boundaries**: Expects Project already in MappingContext (cross-aggregate reference)
 * - **No Orchestration**: Child entities (Commits, Branches) are mapped by assembler
 *
 * ## Usage
 * Prefer using [RepositoryAssembler] at the service layer. This mapper is called by the assembler
 * and is also used for `refreshDomain` operations after persistence.
 *
 * @see com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
 */
@Component
internal class RepositoryMapper : EntityMapper<Repository, RepositoryEntity> {
    @Autowired
    private lateinit var projectRepository: com.inso_world.binocular.infrastructure.sql.persistence.repository.ProjectRepository

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Repository domain object to RepositoryEntity.
     *
     * @param domain The Repository domain object to convert
     * @return The RepositoryEntity
     */
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun toEntity(domain: Repository): RepositoryEntity {
        val owner = projectRepository.findByIid(domain.projectId.value)
            ?: throw IllegalStateException("ProjectEntity with iid ${domain.projectId} not found")

        val entity = domain.toSqlEntity(owner)
        return entity
    }

    /**
     * Converts a RepositoryEntity to Repository domain object.
     *
     * @param entity The RepositoryEntity to convert
     * @return The Repository domain object
     */
    override fun toDomain(entity: RepositoryEntity): Repository {
        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        return domain
    }

    fun refreshDomain(
        target: Repository,
        entity: RepositoryEntity,
    ): Repository {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )

        return target
    }
}

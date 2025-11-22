package com.inso_world.binocular.infrastructure.sql.assembler

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Assembler for the Project aggregate.
 *
 * Orchestrates the mapping of Project domain objects to ProjectEntity persistence entities,
 * including its owned Repository child aggregate. Project is the root aggregate that owns
 * all SCM-related data through its Repository.
 *
 * ## Aggregate Structure
 * ```
 * Project (Root Aggregate)
 *   └── Repository (Owned Secondary Aggregate)
 *         ├── Commit* (owned children)
 *         ├── Branch* (owned children)
 *         └── User* (owned children)
 * ```
 *
 * ## Responsibilities
 * - Convert Project domain to ProjectEntity using ProjectMapper
 * - Orchestrate assembly of owned Repository (via RepositoryAssembler)
 * - Wire Repository to Project maintaining bidirectional relationship
 * - Manage MappingContext to ensure identity preservation
 *
 * ## Design Notes
 * Project is the root aggregate that fully owns Repository. When assembling a Project,
 * the complete object graph including Repository and all its children is built.
 * This ensures identity preservation throughout the entire aggregate.
 */
@Component
internal class ProjectAssembler {
    companion object {
        private val logger by logger()
    }

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    private lateinit var ctx: MappingContext

    /**
     * Assembles a complete ProjectEntity from a Project domain aggregate.
     *
     * This method assembles the entire Project aggregate including its owned Repository
     * and all Repository children (Commits, Branches, Users). The result is a fully
     * identity-preserving object graph.
     *
     * ## Process
     * 1. Check if Project already assembled (identity preservation)
     * 2. Map Project structure using ProjectMapper (adds to context)
     * 3. If Repository exists, assemble it completely using RepositoryAssembler
     * 4. Wire Repository to Project entity
     *
     * @param domain The Project domain aggregate to assemble
     * @return The fully assembled ProjectEntity with Repository and all children
     */
    fun toEntity(domain: Project): ProjectEntity {
        logger.debug("Assembling ProjectEntity for project: ${domain.name}")

        // Fast-path: Check if already assembled (identity preservation)
        ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)?.let {
            logger.trace("Project already in context, returning cached entity")
            return it
        }

        // Phase 1: Map Project structure (adds to context)
        val entity = projectMapper.toEntity(domain)
        logger.trace("Mapped Project structure: id=${entity.id}")

        // Phase 2: Assemble owned Repository if present
        domain.repo?.let { repository ->
            logger.trace("Assembling owned Repository for Project")
            val repoEntity = repositoryAssembler.toEntity(repository)
            entity.repo = repoEntity
            logger.trace("Wired Repository to Project: repoId=${repoEntity.id}")
        }

        logger.debug("Assembled ProjectEntity with id=${entity.id}, hasRepository=${entity.repo != null}")
        return entity
    }

    /**
     * Assembles a complete Project domain aggregate from a ProjectEntity.
     *
     * This method assembles the entire Project aggregate including its owned Repository
     * and all Repository children. The result is a fully identity-preserving object graph.
     *
     * ## Process
     * 1. Check if Project already assembled (identity preservation)
     * 2. Map Project structure using ProjectMapper (adds to context)
     * 3. If RepositoryEntity exists, assemble it completely using RepositoryAssembler
     * 4. Wire Repository to Project domain
     *
     * @param entity The ProjectEntity to convert
     * @return The fully assembled Project domain aggregate
     */
    fun toDomain(entity: ProjectEntity): Project {
        logger.debug("Assembling Project domain for entity id=${entity.id}")

        // Fast-path: Check if already assembled (identity preservation)
        ctx.findDomain<Project, ProjectEntity>(entity)?.let {
            logger.trace("Project already in context, returning cached domain")
            return it
        }

        // Phase 1: Map Project structure (adds to context)
        val domain = projectMapper.toDomain(entity)
        logger.trace("Mapped Project structure: ${domain.name}")

        // Phase 2: Assemble owned Repository if present
        entity.repo?.let { repoEntity ->
            logger.trace("Assembling owned Repository from ProjectEntity")
            val repository = repositoryAssembler.toDomain(repoEntity)
            domain.repo = repository
            logger.trace("Wired Repository to Project: ${repository.localPath}")
        }

        logger.debug("Assembled Project domain: ${domain.name}, hasRepository=${domain.repo != null}")
        return domain
    }
}

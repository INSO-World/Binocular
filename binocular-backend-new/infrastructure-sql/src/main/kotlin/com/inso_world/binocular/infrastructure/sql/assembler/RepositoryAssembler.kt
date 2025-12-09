package com.inso_world.binocular.infrastructure.sql.assembler

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Assembler for the Repository aggregate.
 *
 * Orchestrates the complete mapping of Repository aggregate including all owned entities
 * (Commits, Branches, Users) while maintaining a reference to its parent Project aggregate.
 * Repository is a secondary aggregate owned by Project, responsible for all SCM-related data.
 *
 * ## Aggregate Structure
 * ```
 * Repository (Secondary Aggregate, owned by Project)
 *   ├── Commit* (owned children)
 *   │     ├── parents → Commit* (graph relationships)
 *   │     └── children → Commit* (graph relationships)
 *   ├── Branch* (owned children)
 *   ├── User* (owned children)
 *   └── → Project (parent aggregate reference)
 * ```
 *
 * ## Responsibilities
 * - Convert Repository domain to RepositoryEntity using RepositoryMapper
 * - Orchestrate mapping of all child entities (Commits, Branches, Users)
 * - Wire bidirectional relationships within the aggregate:
 *   - Commit author/committer relationships
 *   - Commit parent/child graph relationships (two-pass assembly)
 *   - Branch head references
 * - Manage parent Project reference (creates minimal reference if not in context)
 * - Coordinate with MappingContext to ensure identity preservation
 *
 * ## Design Principles
 * - **Identity Preservation**: Returns identity-preserving objects for all children
 * - **Aggregate Boundaries**: Does NOT fully build parent Project when assembled standalone
 * - **Parent Reference**: If Project not in context, creates minimal Project structure (no Repository child)
 * - **Top-Down Mapping**: Repository controls mapping of its owned children
 * - **Two-Pass Graph Assembly**: Commits mapped first, then parent/child relationships wired second
 * - **Context-Aware**: Uses MappingContext for identity map pattern
 * - **Separation of Concerns**: Mappers do simple conversion, assembler orchestrates
 *
 * ## Usage Scenarios
 *
 * ### Scenario 1: Assembled via ProjectAssembler (typical)
 * ```kotlin
 * // Project is root aggregate, assembles everything
 * val projectEntity = projectAssembler.toEntity(project)
 * // Project is already in context when Repository is assembled
 * ```
 *
 * ### Scenario 2: Assembled standalone
 * ```kotlin
 * // Repository assembled independently (e.g., for partial updates)
 * val repositoryEntity = repositoryAssembler.toEntity(repository)
 * // Creates minimal Project reference, doesn't build full Project aggregate
 * ```
 */
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
    @Lazy
    private lateinit var remoteMapper: RemoteMapper

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    private lateinit var ctx: MappingContext

    /**
     * Assembles a complete RepositoryEntity from a Repository domain aggregate.
     *
     * This method assembles the Repository and all its owned children (Commits, Branches, Users)
     * with full identity preservation. It ensures a Project reference exists but does NOT
     * fully build the parent Project aggregate when assembled standalone.
     *
     * ## Process
     * 1. Check if Repository already assembled (identity preservation)
     * 2. Ensure Project reference exists in context:
     *    - If found: reuse existing (typical when called via ProjectAssembler)
     *    - If not found: create minimal Project structure without Repository child
     * 3. Map Repository structure using RepositoryMapper
     * 4. Map all Commits (first pass):
     *    - Convert Commit → CommitEntity using CommitMapper
     *    - Wire author/committer relationships
     *    - Add to RepositoryEntity
     * 5. Wire commit parent/child relationships (second pass):
     *    - Lookup all commits in MappingContext
     *    - Wire bidirectional parent/child graph
     * 6. Map all Branches and wire to RepositoryEntity
     *
     * @param domain The Repository domain aggregate to assemble
     * @return The fully assembled RepositoryEntity with all children and identity preservation
     */
    fun toEntity(domain: Repository): RepositoryEntity {
        logger.trace("Assembling RepositoryEntity for repository: ${domain.localPath}")

        // Fast-path: Check if already assembled (identity preservation)
//        ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)?.let {
//            logger.debug("Repository already in context, returning cached entity")
//            return it
//        }

        // Ensure Project reference exists in context (but don't assemble Repository child)
//        val projectEntity = requireNotNull(
//            ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
//        ) {
//            "ProjectEntity must be in context to assemble repository"
//        }
//            ?: run {
//                logger.trace("Project not in context, mapping minimal Project structure (no Repository child)")
//                projectMapper.toEntity(domain.project)
//            }
//        logger.debug("Project reference in context: id=${projectEntity.id}")

        // Phase 1: Map Repository structure (without children)
        val entity = repositoryMapper.toEntity(domain)
        logger.debug("Mapped Repository structure: id=${entity.id}")

        // Phase 2: Map Commits (developer signatures handled inside CommitMapper)
        logger.debug("Mapping ${domain.commits.size} commits")
        domain.commits.forEach { commit ->
            val commitEntity = commitMapper.toEntity(commit)
            entity.commits.add(commitEntity)
        }

        // Phase 2b: Wire parent/child commit relationships (second pass)
        logger.debug("Wiring parent/child relationships for ${domain.commits.size} commits")
        domain.commits.forEach { commit ->
            val commitEntity = ctx.findEntity<Commit.Key, Commit, CommitEntity>(commit)
                ?: throw IllegalStateException("CommitEntity for ${commit.sha} must be in context")

            commit.parents.forEach { parentCommit ->
                val parentEntity = ctx.findEntity<Commit.Key, Commit, CommitEntity>(parentCommit)
                    ?: throw IllegalStateException("Parent CommitEntity for ${parentCommit.sha} must be in context")

                // Wire bidirectional relationship (only if not already present)
                if (!commitEntity.parents.contains(parentEntity)) {
                    commitEntity.parents.add(parentEntity)
                    parentEntity.children.add(commitEntity)
                }
            }
        }

        // Phase 3: Map and wire Branches
        logger.debug("Mapping ${domain.branches.size} branches")
        domain.branches.forEach { branch ->
            val branchEntity = branchMapper.toEntity(branch)
            entity.branches.add(branchEntity)
        }

        // Phase 4: Map and wire Remotes
        logger.debug("Mapping ${domain.remotes.size} remotes")
        domain.remotes.forEach { remote ->
            val remoteEntity = remoteMapper.toEntity(remote)
            entity.remotes.add(remoteEntity)
        }

        // Phase 5: Map and wire Developers
        logger.debug("Mapping ${domain.developers.size} developers")
        domain.developers.forEach { developer ->
            val developerEntity = developerMapper.toEntity(developer)
            entity.developers.add(developerEntity)
        }

        logger.trace(
            "Assembled RepositoryEntity: id=${entity.id}, " +
                    "commits=${entity.commits.size}, branches=${entity.branches.size}, remotes=${entity.remotes.size}, developers=${entity.developers.size}"
        )

        return entity
    }

    /**
     * Assembles a complete Repository domain aggregate from a RepositoryEntity.
     *
     * This method assembles the Repository and all its owned children (Commits, Branches, Users)
     * with full identity preservation. It ensures a Project reference exists but does NOT
     * fully build the parent Project aggregate when assembled standalone.
     *
     * ## Process
     * 1. Check if Repository already assembled (identity preservation)
     * 2. Ensure Project reference exists in context:
     *    - If found: reuse existing (typical when called via ProjectAssembler)
     *    - If not found: create minimal Project structure without Repository child
     * 3. Map Repository structure using RepositoryMapper
     * 4. Map all Commits (first pass):
     *    - Convert CommitEntity → Commit using CommitMapper
     *    - Wire author/committer relationships
     *    - Add to Repository
     * 5. Wire commit parent/child relationships (second pass):
     *    - Lookup all commits in MappingContext
     *    - Wire bidirectional parent/child graph
     *    - Note: Domain Commit.parents.add() automatically maintains bidirectionality
     * 6. Map all BranchEntities to Branches and add to Repository
     *
     * @param entity The RepositoryEntity to convert
     * @return The fully assembled Repository domain aggregate with identity preservation
     */
    fun toDomain(entity: RepositoryEntity): Repository {
        logger.trace("Assembling Repository domain for entity id=${entity.id}")

        // Fast-path: Check if already assembled (identity preservation)
        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let {
            logger.debug("Repository already in context, returning cached domain")
            return it
        }

        // Ensure Project reference exists in context (but don't assemble Repository child)
        val project = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: run {
                logger.debug("Project not in context, mapping minimal Project structure (no Repository child)")
                projectMapper.toDomain(entity.project)
            }

        logger.debug("Project reference in context: ${project.name}")

        // Phase 2: Map Repository structure
        val domain = repositoryMapper.toDomain(entity)
        logger.debug("Mapped Repository structure: ${domain.localPath}")

        // Phase 3: Map Developers
        logger.debug("Mapping ${entity.developers.size} developers")
        entity.developers.forEach { developerEntity ->
            val developer = developerMapper.toDomain(developerEntity)
            domain.developers.add(developer)
        }

        // Phase 4: Map Commits (developers/signatures handled by mapper)
        logger.debug("Mapping ${entity.commits.size} commits")
        entity.commits.forEach { commitEntity ->
            val commit = commitMapper.toDomain(commitEntity)
            domain.commits.add(commit)
            domain.developers.add(commit.author)
            domain.developers.add(commit.committer)
        }

        // Phase 4b: Wire parent/child commit relationships (second pass)
        logger.debug("Wiring parent/child relationships for ${entity.commits.size} commits")
        entity.commits.forEach { commitEntity ->
            val commit = ctx.findDomain<Commit, CommitEntity>(commitEntity)
                ?: throw IllegalStateException("Commit for ${commitEntity.sha} must be in context")

            commitEntity.parents.forEach { parentEntity ->
                val parentCommit = ctx.findDomain<Commit, CommitEntity>(parentEntity)
                    ?: throw IllegalStateException("Parent Commit for ${parentEntity.sha} must be in context")

                if (!commit.parents.contains(parentCommit)) {
                    commit.parents.add(parentCommit)
                }
            }
        }

        // Phase 5: Map and wire Branches
        logger.debug("Mapping ${entity.branches.size} branches")
        entity.branches.forEach { branchEntity ->
            val branch = branchMapper.toDomain(branchEntity)
            domain.branches.add(branch)
        }

        // Phase 6: Map and wire Remotes
        logger.debug("Mapping ${entity.remotes.size} remotes")
        entity.remotes.forEach { remoteEntity ->
            val remote = remoteMapper.toDomain(remoteEntity)
            domain.remotes.add(remote)
        }

        logger.trace(
            "Assembled Repository domain: ${domain.localPath}, " +
                    "commits=${domain.commits.size}, branches=${domain.branches.size}, remotes=${domain.remotes.size}"
        )

        return domain
    }

    fun refresh(domain: Repository, entity: RepositoryEntity) : Repository {
        logger.trace("Refreshing Repository domain: ${domain.iid}")
        this.repositoryMapper.refreshDomain(domain, entity)

        with(entity.commits.associateBy(CommitEntity::iid)) {
            domain.commits.parallelStream().forEach { commit ->
                this@RepositoryAssembler.commitMapper.refreshDomain(commit, this.getValue(commit.iid))
            }
        }

        with(entity.branches.associateBy(BranchEntity::iid)) {
            domain.branches.parallelStream().forEach { branch ->
                this@RepositoryAssembler.branchMapper.refreshDomain(branch, this.getValue(branch.iid))
            }
        }

        with(entity.remotes.associateBy(RemoteEntity::iid)) {
            domain.remotes.parallelStream().forEach { remotes ->
                this@RepositoryAssembler.remoteMapper.refreshDomain(remotes, this.getValue(remotes.iid))
            }
        }

        with(entity.developers.associateBy(DeveloperEntity::iid)) {
            domain.developers.forEach { developer ->
                this@RepositoryAssembler.developerMapper.refreshDomain(developer, this.getValue(developer.iid))
            }
        }

        return domain
    }
}

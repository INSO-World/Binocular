package com.inso_world.binocular.infrastructure.arangodb.assembler

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Assembler for the Repository aggregate.
 *
 * Orchestrates the complete mapping of Repository aggregate including all owned entities
 * (Commits, Branches, Users) for ArangoDB while maintaining a reference to its parent Project aggregate.
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
 *
 * ## ArangoDB-Specific Notes
 * - ArangoDB uses document-based storage with references
 * - Parent/child commit relationships are managed via commit SHA references
 * - No separate Remote entities (not applicable for ArangoDB implementation)
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
    private lateinit var userMapper: UserMapper

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
     *    - Store in ArangoDB (commits are added to repository context)
     * 5. Wire commit parent/child relationships (second pass):
     *    - Note: For ArangoDB, parent/child relationships are managed via commit SHAs
     *    - Relationships are typically lazy-loaded from the document store
     * 6. Map all Branches and wire to RepositoryEntity
     *
     * ## ArangoDB Storage Note
     * In ArangoDB, commits and branches are stored as separate documents with references.
     * The assembler prepares the complete structure but actual relationship storage
     * is handled by the ArangoDB persistence layer.
     *
     * @param domain The Repository domain aggregate to assemble
     * @return The fully assembled RepositoryEntity with all children and identity preservation
     */
    fun toEntity(domain: Repository): RepositoryEntity {
        logger.debug("Assembling RepositoryEntity for repository: ${domain.localPath}")

        // Fast-path: Check if already assembled (identity preservation)
        ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)?.let {
            logger.trace("Repository already in context, returning cached entity")
            return it
        }

        // Ensure Project reference exists in context (but don't assemble Repository child)
        val projectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
            ?: run {
                logger.trace("Project not in context, mapping minimal Project structure (no Repository child)")
                projectMapper.toEntity(domain.project)
            }

        logger.trace("Project reference in context: id=${projectEntity.id}")

        // Phase 1: Map Repository structure (without children)
        val entity = repositoryMapper.toEntity(domain)
        logger.trace("Mapped Repository structure: id=${entity.id}")

        // Phase 2: Map Commits
        // Note: In ArangoDB, commits are stored as separate documents
        // The mapper handles the conversion, and parent/child relationships
        // are managed through commit SHA references in the document store
        logger.trace("Mapping ${domain.commits.size} commits")
        domain.commits.forEach { commit ->
            // Map commit structure - this adds it to the MappingContext
            commitMapper.toEntity(commit)

            // Map commit authors and committers
            commit.author?.let { author ->
                userMapper.toEntity(author)
            }

            userMapper.toEntity(commit.committer)
        }

        // Phase 3: Map Branches
        // Note: In ArangoDB, branches reference commits by SHA
        logger.trace("Mapping ${domain.branches.size} branches")
        domain.branches.forEach { branch ->
            branchMapper.toEntity(branch)
        }

        logger.debug(
            "Assembled RepositoryEntity: id=${entity.id}, " +
                    "commits=${domain.commits.size}, branches=${domain.branches.size}"
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
     * 4. Note: For ArangoDB, commits, branches, and users are lazy-loaded from the document store
     *    - The RepositoryEntity doesn't eagerly load all children
     *    - Relationships are resolved on-demand through ArangoDB queries
     *
     * ## ArangoDB Loading Note
     * In ArangoDB, the Repository aggregate uses lazy loading for its children.
     * Unlike SQL where we eagerly load all commits/branches, ArangoDB loads
     * these on-demand when accessed. This is more efficient for large repositories.
     *
     * @param entity The RepositoryEntity to convert
     * @return The fully assembled Repository domain aggregate with identity preservation
     */
    fun toDomain(entity: RepositoryEntity): Repository {
        logger.debug("Assembling Repository domain for entity id=${entity.id}")

        // Fast-path: Check if already assembled (identity preservation)
        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let {
            logger.trace("Repository already in context, returning cached domain")
            return it
        }

        // Ensure Project reference exists in context (but don't assemble Repository child)
        val project = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: run {
                logger.trace("Project not in context, mapping minimal Project structure (no Repository child)")
                projectMapper.toDomain(entity.project)
            }

        logger.trace("Project reference in context: ${project.name}")

        // Phase 1: Map Repository structure
        val domain = repositoryMapper.toDomain(entity)
        logger.trace("Mapped Repository structure: ${domain.localPath}")

        // Note: In ArangoDB, commits, branches, and users are lazy-loaded
        // The domain object is returned with the structure in place,
        // and children will be loaded on-demand when accessed

        logger.debug("Assembled Repository domain: ${domain.localPath}")

        return domain
    }
}

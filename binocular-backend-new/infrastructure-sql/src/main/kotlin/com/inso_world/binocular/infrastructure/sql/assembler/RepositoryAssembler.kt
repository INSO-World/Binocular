package com.inso_world.binocular.infrastructure.sql.assembler

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import kotlin.uuid.ExperimentalUuidApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@OptIn(ExperimentalUuidApi::class)
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

    fun toEntity(domain: Repository): RepositoryEntity {
        logger.trace("Assembling RepositoryEntity for repository: ${domain.localPath}")

        val entity = repositoryMapper.toEntity(domain)
        logger.debug("Mapped Repository structure: id=${entity.id}")

        logger.debug("Mapping commits for repository")
        domain.commitIds.forEach { commitId ->
            val commitEntity = ctx.findEntityByIid<CommitEntity>(commitId, Commit::class)
                ?: throw IllegalStateException("CommitEntity for ${commitId} must be in context")
            entity.commits.add(commitEntity)
        }

        logger.debug("Wiring parent/child relationships")
        entity.commits.forEach { commitEntity ->
            val commit = ctx.findDomain<Commit, CommitEntity>(commitEntity)
                ?: throw IllegalStateException("Commit for ${commitEntity.sha} must be in context")

            commit.parentIds.forEach { parentId ->
                val parentEntity = ctx.findEntityByIid<CommitEntity>(parentId, Commit::class)
                    ?: throw IllegalStateException("Parent CommitEntity for ${parentId} must be in context")

                if (!commitEntity.parents.contains(parentEntity)) {
                    commitEntity.parents.add(parentEntity)
                    parentEntity.children.add(commitEntity)
                }
            }
        }

        logger.debug("Mapping branches")
        domain.branchIds.forEach { branchId ->
            val branch = ctx.findDomainByIid<Branch>(branchId, BranchEntity::class)
                ?: throw IllegalStateException("Branch for ${branchId} must be in context")
            val branchEntity = branchMapper.toEntity(branch)
            entity.branches.add(branchEntity)
        }

        logger.debug("Mapping remotes")
        domain.remoteIds.forEach { remoteId ->
            val remote = ctx.findDomainByIid<Remote>(remoteId, RemoteEntity::class)
                ?: throw IllegalStateException("Remote for ${remoteId} must be in context")
            val remoteEntity = remoteMapper.toEntity(remote)
            entity.remotes.add(remoteEntity)
        }

        logger.debug("Mapping developers")
        domain.developers.forEach { developerId ->
            val developer = ctx.findDomainByIid<Developer>(developerId, DeveloperEntity::class)
                ?: throw IllegalStateException("Developer for ${developerId} must be in context")
            val developerEntity = developerMapper.toEntity(developer)
            entity.developers.add(developerEntity)
        }

        logger.trace(
            "Assembled RepositoryEntity: id=${entity.id}, " +
                    "commits=${entity.commits.size}, branches=${entity.branches.size}, remotes=${entity.remotes.size}, developers=${entity.developers.size}"
        )

        return entity
    }

    fun toDomain(entity: RepositoryEntity): Repository {
        logger.trace("Assembling Repository domain for entity id=${entity.id}")

        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let {
            logger.debug("Repository already in context, returning cached domain")
            return it
        }

        val project = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: run {
                logger.debug("Project not in context, mapping minimal Project structure (no Repository child)")
                projectMapper.toDomain(entity.project)
            }

        logger.debug("Project reference in context: ${project.name}")

        val domain = repositoryMapper.toDomain(entity)
        logger.debug("Mapped Repository structure: ${domain.localPath}")

        logger.debug("Mapping ${entity.developers.size} developers")
        entity.developers.forEach { developerEntity ->
            val developer = developerMapper.toDomain(developerEntity)
            domain.developers.add(developer.iid)
        }

        logger.debug("Mapping ${entity.commits.size} commits")
        entity.commits.forEach { commitEntity ->
            val commit = commitMapper.toDomain(commitEntity)
            domain.commitIds.add(commit.iid)
            domain.developers.add(commit.authorId)
            domain.developers.add(commit.committerId)
        }

        logger.debug("Wiring parent/child relationships for ${entity.commits.size} commits")
        entity.commits.forEach { commitEntity ->
            val commit = ctx.findDomain<Commit, CommitEntity>(commitEntity)
                ?: throw IllegalStateException("Commit for ${commitEntity.sha} must be in context")

            commitEntity.parents.forEach { parentEntity ->
                val parentCommit = ctx.findDomain<Commit, CommitEntity>(parentEntity)
                    ?: throw IllegalStateException("Parent Commit for ${parentEntity.sha} must be in context")

                val parentIid = parentCommit.iid
                if (!commit.parentIds.contains(parentIid)) {
                    commit.parentIds.add(parentIid)
                }
            }
        }

        logger.debug("Mapping ${entity.branches.size} branches")
        entity.branches.forEach { branchEntity ->
            val branch = branchMapper.toDomain(branchEntity)
            domain.branchIds.add(Branch.Id(branch.iid.value))
        }

        logger.debug("Mapping ${entity.remotes.size} remotes")
        entity.remotes.forEach { remoteEntity ->
            val remote = remoteMapper.toDomain(remoteEntity)
            domain.remoteIds.add(remote.iid)
        }

        logger.trace(
            "Assembled Repository domain: ${domain.localPath}, " +
                    "commits=${domain.commitIds.size}, branches=${domain.branchIds.size}, remotes=${domain.remoteIds.size}"
        )

        return domain
    }

    fun refresh(domain: Repository, entity: RepositoryEntity): Repository {
        logger.trace("Refreshing Repository domain: ${domain.iid}")
        this.repositoryMapper.refreshDomain(domain, entity)

        val commitsMap = entity.commits.associateBy { it.iid }
        domain.commitIds.forEach { commitId ->
            val commitEntity = commitsMap[commitId]
                ?: throw IllegalStateException("CommitEntity for ${commitId} not found")
            val commit = ctx.findDomain<Commit, CommitEntity>(commitEntity)
                ?: throw IllegalStateException("Commit for ${commitId} must be in context")
            this.commitMapper.refreshDomain(commit, commitEntity)
        }

        val branchesMap = entity.branches.associateBy { it.iid.value }
        domain.branchIds.forEach { branchId ->
            val branchEntity = branchesMap[branchId.value]
                ?: throw IllegalStateException("BranchEntity for ${branchId} not found")
            val branch = ctx.findDomain<Branch, BranchEntity>(branchEntity)
                ?: throw IllegalStateException("Branch for ${branchId} must be in context")
            this.branchMapper.refreshDomain(branch, branchEntity)
        }

        val remotesMap = entity.remotes.associateBy { it.iid }
        domain.remoteIds.forEach { remoteId ->
            val remoteEntity = remotesMap[remoteId]
                ?: throw IllegalStateException("RemoteEntity for ${remoteId} not found")
            val remote = ctx.findDomain<Remote, RemoteEntity>(remoteEntity)
                ?: throw IllegalStateException("Remote for ${remoteId} must be in context")
            this.remoteMapper.refreshDomain(remote, remoteEntity)
        }

        val developersMap = entity.developers.associateBy { it.iid }
        domain.developers.forEach { developerId ->
            val developerEntity = developersMap[developerId]
                ?: throw IllegalStateException("DeveloperEntity for ${developerId} not found")
            val developer = ctx.findDomain<Developer, DeveloperEntity>(developerEntity)
                ?: throw IllegalStateException("Developer for ${developerId} must be in context")
            this.developerMapper.refreshDomain(developer, developerEntity)
        }

        return domain
    }
}
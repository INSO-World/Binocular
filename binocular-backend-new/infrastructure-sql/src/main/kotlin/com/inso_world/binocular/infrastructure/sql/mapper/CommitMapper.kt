package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class CommitMapper : EntityMapper<Commit, CommitEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var developerMapper: DeveloperMapper

    companion object {
        private val logger by logger()
    }

    override fun toEntity(domain: Commit): CommitEntity {
        ctx.findEntity<Commit.Key, Commit, CommitEntity>(domain)?.let { return it }

        val repo = ctx.findDomainByIid<Repository>(domain.repositoryId, RepositoryEntity::class)
            ?: throw IllegalStateException(
                "Repository must be mapped before Commit. " +
                        "Ensure Repository is in MappingContext before calling toEntity()."
            )
        val owner = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repo)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before CommitEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity()."
            )

        val author = ctx.findDomainByIid<Developer>(domain.authorSignature.developerId, DeveloperEntity::class)
            ?: throw IllegalStateException(
                "Author Developer must be mapped before Commit. " +
                        "Ensure Developer is in MappingContext before calling toEntity()."
            )
        val authorEntity = ctx.findEntity<Developer.Key, Developer, DeveloperEntity>(author)
            ?: throw IllegalStateException(
                "Author DeveloperEntity must be mapped before CommitEntity. " +
                        "Ensure DeveloperEntity is in MappingContext before calling toEntity()."
            )
        val committer = ctx.findDomainByIid<Developer>(domain.committerSignature.developerId, DeveloperEntity::class)
            ?: throw IllegalStateException(
                "Committer Developer must be mapped before Commit. " +
                        "Ensure Developer is in MappingContext before calling toEntity()."
            )
        val committerEntity = ctx.findEntity<Developer.Key, Developer, DeveloperEntity>(committer)
            ?: throw IllegalStateException(
                "Committer DeveloperEntity must be mapped before CommitEntity. " +
                        "Ensure DeveloperEntity is in MappingContext before calling toEntity()."
            )

        val entity = domain.toEntity(
            repository = owner,
            author = authorEntity,
            committer = committerEntity,
        )
        ctx.remember(domain, entity)

        return entity
    }

    override fun toDomain(entity: CommitEntity): Commit {
        ctx.findDomain<Commit, CommitEntity>(entity)?.let { return it }

        val owner = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Commit. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        val author = developerMapper.toDomain(entity.author)
        val committer = developerMapper.toDomain(entity.committer)

        val domain = entity.toDomain(owner, author, committer)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )
        ctx.remember(domain, entity)

        return domain
    }

    fun refreshDomain(target: Commit, entity: CommitEntity): Commit {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )

        return target
    }
}
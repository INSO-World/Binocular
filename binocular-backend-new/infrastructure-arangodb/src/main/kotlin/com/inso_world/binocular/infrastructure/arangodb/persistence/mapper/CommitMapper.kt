package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Signature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi

@Component
internal class CommitMapper : EntityMapper<Commit, CommitEntity> {

    @Autowired
    private lateinit var developerMapper: DeveloperMapper

    @OptIn(ExperimentalUuidApi::class)
    override fun toEntity(domain: Commit): CommitEntity {
        val owner = RepositoryEntity(
            id = null,
            iid = domain.repositoryId.value,
            localPath = "",
            projectId = "",
        )
        val author = domain.authorSignature.toDeveloper()
        val committer = domain.committerSignature.toDeveloper()
        val authorEntity = developerMapper.toEntity(author)
        val committerEntity = developerMapper.toEntity(committer)

        return domain.toEntity(owner, authorEntity, committerEntity)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun toDomain(entity: CommitEntity): Commit {
        val repoId = com.inso_world.binocular.model.Repository.Id(kotlin.uuid.Uuid.parse(entity.repositoryId))

        val authorEntity = DeveloperEntity(
            id = entity.authorId,
            gitSignature = "",
            iid = com.inso_world.binocular.model.Developer.Id(kotlin.uuid.Uuid.parse(entity.authorId)),
        )
        val committerEntity = DeveloperEntity(
            id = entity.committerId,
            gitSignature = "",
            iid = com.inso_world.binocular.model.Developer.Id(kotlin.uuid.Uuid.parse(entity.committerId)),
        )

        val author = authorEntity.toDomain()
        val committer = committerEntity.toDomain()

        val authorSignature = Signature(
            developerId = author.iid,
            gitSignature = author.gitSignature,
            timestamp = entity.authorDateTime,
        )
        val committerSignature =
            if (committer.iid == author.iid && entity.commitDateTime == entity.authorDateTime) {
                authorSignature
            } else {
                Signature(
                    developerId = committer.iid,
                    gitSignature = committer.gitSignature,
                    timestamp = entity.commitDateTime,
                )
            }

        val domain = entity.toDomain(repoId, authorSignature, committerSignature)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid,
        )
        return domain
    }

    fun refreshDomain(target: Commit, entity: CommitEntity): Commit {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id,
        )
        return target
    }
}

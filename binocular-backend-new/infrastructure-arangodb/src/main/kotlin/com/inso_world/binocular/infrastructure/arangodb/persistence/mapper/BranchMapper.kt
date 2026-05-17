package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Branch
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi

@Component
internal class BranchMapper : EntityMapper<Branch, BranchEntity> {

    @OptIn(ExperimentalUuidApi::class)
    override fun toEntity(domain: Branch): BranchEntity {
        val owner = RepositoryEntity(
            id = null,
            iid = domain.repositoryId.value,
            localPath = "",
            projectId = "",
        )
        val head = CommitEntity(
            id = null,
            sha = "",
            iid = domain.headCommitId.value,
            authorDateTime = java.time.LocalDateTime.now(),
            commitDateTime = java.time.LocalDateTime.now(),
            repositoryId = "",
            authorId = "",
            committerId = "",
        )
        return domain.toEntity(owner, head)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun toDomain(entity: BranchEntity): Branch {
        val repoId = com.inso_world.binocular.model.Repository.Id(kotlin.uuid.Uuid.parse(entity.repositoryId))
        val headId = com.inso_world.binocular.model.Commit.Id(kotlin.uuid.Uuid.parse(entity.headCommitId))
        val domain = entity.toDomain(repoId, headId)
        setField(
            domain.javaClass.superclass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid,
        )
        return domain
    }

    fun refreshDomain(target: Branch, entity: BranchEntity): Branch {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id,
        )
        return target
    }
}

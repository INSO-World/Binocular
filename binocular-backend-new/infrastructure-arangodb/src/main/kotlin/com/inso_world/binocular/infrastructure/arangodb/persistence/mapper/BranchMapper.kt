package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toArangoEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Branch domain objects.
 *
 * Converts between Branch domain objects and BranchEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Branch structure
 * - **No Deep Traversal**: Does not map entire commit history or file structures
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. Direct usage
 * is also supported for `refreshDomain` operations after persistence.
 */
@Component
internal class BranchMapper : EntityMapper<Branch, BranchEntity> {

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Branch domain object to BranchEntity.
     *
     * @param domain The Branch domain object to convert
     * @return The BranchEntity (structure only)
     */
    @OptIn(ExperimentalUuidApi::class)
    override fun toEntity(domain: Branch): BranchEntity {
        val owner = RepositoryEntity(
            iid = domain.repositoryId.value,
            localPath = "",
            project = ProjectEntity(iid = domain.repositoryId.value, name = "")
        )
        val head = CommitEntity(
            iid = Uuid.random(), // Dummy iid
            sha = domain.headSha,
            authorDateTime = LocalDateTime.now(),
            commitDateTime = LocalDateTime.now(),
            repository = owner,
            author = DeveloperEntity(iid = Developer.Id(Uuid.random()), gitSignature = "", repository = owner),
            committer = DeveloperEntity(iid = Developer.Id(Uuid.random()), gitSignature = "", repository = owner)
        )

        val entity = domain.toArangoEntity(owner, head)

        return entity
    }

    /**
     * Converts a BranchEntity to Branch domain object.
     *
     * @param entity The BranchEntity to convert
     * @return The Branch domain object (structure only)
     */
    @OptIn(ExperimentalUuidApi::class)
    override fun toDomain(entity: BranchEntity): Branch {
        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.superclass
                .getDeclaredField("iid"),
            domain,
            entity.iid
        )

        return domain
    }

    /**
     * Refreshes a Branch domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It does NOT update nested objects - only top-level Branch properties.
     *
     * @param target The Branch domain object to refresh
     * @param entity The BranchEntity with updated data
     * @return The refreshed Branch domain object
     */
    fun refreshDomain(
        target: Branch,
        entity: BranchEntity,
    ): Branch {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id
        )
        return target
    }
}

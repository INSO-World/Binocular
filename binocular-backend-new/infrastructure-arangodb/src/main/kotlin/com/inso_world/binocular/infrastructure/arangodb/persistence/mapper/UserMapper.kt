package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for User domain objects.
 *
 * Converts between User domain objects and UserEntity persistence entities for ArangoDB.
 * This mapper intentionally keeps the conversion shallow; it does not traverse commit graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts User structure
 * - **No Deep Traversal**: Does not automatically map commit relationships
 * - **Context-Dependent**: Requires Repository already in MappingContext
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. The `toDomain`
 * method requires the Repository to already exist in MappingContext to preserve the
 * repository reference.
 *
 * @deprecated Use [DeveloperMapper] instead. This mapper is maintained for backwards compatibility
 * with the deprecated [User] model.
 */
@Suppress("DEPRECATION")
@Component
internal class UserMapper : EntityMapper<User, UserEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a User domain object to UserEntity.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Repository must be handled first.
     *
     * **Note**: This method does NOT map commit relationships or other deep structures.
     * Use assemblers for complete graph assembly.
     *
     * @param domain The User domain object to convert
     * @return The UserEntity (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toEntity(domain: User): UserEntity {
        ctx.findEntity<User.Key, User, UserEntity>(domain)?.let { return it }

        val owner =
            ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
                ?: throw IllegalStateException(
                    "RepositoryEntity must be mapped before UserEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity().",
                )

        val entity = domain.toEntity(owner)
        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a UserEntity to User domain object.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Repository must be handled first.
     *
     * **Note**: This method does NOT map commit relationships or other deep structures.
     * Use assemblers for complete graph assembly.
     *
     * @param entity The UserEntity to convert
     * @return The User domain object (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun toDomain(entity: UserEntity): User {
        ctx.findDomain<User, UserEntity>(entity)?.let { return it }

        val owner =
            ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
                ?: throw IllegalStateException(
                    "Repository must be mapped before User. " +
                        "Ensure Repository is in MappingContext before calling toDomain().",
                )

        val domain = entity.toDomain(owner)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            User.Id(entity.iid)
        )
        ctx.remember(domain, entity)
        return domain
    }

    /**
     * Refreshes a User domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It does NOT update nested objects - only top-level User properties.
     *
     * @param target The User domain object to refresh
     * @param entity The UserEntity with updated data
     * @return The refreshed User domain object
     */
    fun refreshDomain(
        target: User,
        entity: UserEntity,
    ): User {
        if (target.id.equals(entity.id)) {
            return target
        }
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id
        )
        return target
    }
}

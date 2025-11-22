package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Mapper for User domain objects.
 *
 * Converts between User domain objects and UserEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships like commit authorship graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts User structure
 * - **No Deep Traversal**: Does not automatically map entire authored/committed commit graphs
 * - **Context-Dependent toDomain**: Requires User already in context (preserves repository reference)
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. The `toDomain`
 * method requires the User to already exist in MappingContext to preserve the repository
 * reference, which cannot be reconstructed from the entity alone.
 */
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
     * Creates a Git signature string from the user's name and email in the format:
     * "Name <email>" or just "Name" if email is not present.
     *
     * **Note**: This method does NOT map authored/committed commit relationships or other deep structures.
     * Use assemblers for complete user graph assembly.
     *
     * @param domain The User domain object to convert
     * @return The UserEntity (structure only, without commit relationships)
     */
    override fun toEntity(domain: User): UserEntity {
        // Fast-path: if this User was already mapped in the current context, return it.
        ctx.findEntity<User.Key, User, UserEntity>(domain)?.let { return it }

        val signature = buildString {
            append(domain.name)
            domain.email?.let { append(" <").append(it).append('>') }
        }

        val entity =
            UserEntity(
                id = domain.id,
                gitSignature = signature,
            )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a UserEntity to User domain object.
     *
     * **Precondition**: The User must already exist in MappingContext. This is necessary because
     * the UserEntity does not store the repository reference, which is required by the User domain model.
     * The repository reference must be preserved from the original domain object.
     *
     * **Note**: This method does NOT map authored/committed commit relationships or other deep structures.
     * Use assemblers for complete user graph assembly.
     *
     * @param entity The UserEntity to convert
     * @return The User domain object from MappingContext
     * @throws IllegalStateException if User is not already in MappingContext
     */
    override fun toDomain(entity: UserEntity): User {
        // Fast-path: Check if already mapped - required for repository reference
        ctx.findDomain<User, UserEntity>(entity)?.let { return it }

        throw IllegalStateException(
            "User mapping requires existing domain User in MappingContext to supply repository. " +
                    "Ensure User is created with repository before calling toDomain()."
        )
    }

    override fun toDomainList(entities: Iterable<UserEntity>): List<User> = entities.map { toDomain(it) }
}

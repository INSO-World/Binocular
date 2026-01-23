package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for User domain objects.
 *
 * @deprecated Use [DeveloperMapper] instead. This mapper is maintained for backwards compatibility.
 *
 * Converts between User domain objects and UserEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships like commit authorship graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts User structure
 * - **Aggregate Boundaries**: Expects Repository already in MappingContext (cross-aggregate reference)
 * - **No Deep Traversal**: Does not automatically map entire authored/committed commit graphs
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers.
 */
@Deprecated("Use DeveloperMapper instead")
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
     * This enforces aggregate boundary - Repository is a separate aggregate that must be handled first.
     *
     * Creates a Git signature string from the user's name and email in the format:
     * "Name <email>" or just "Name" if email is not present.
     *
     * **Note**: This method does NOT map authored/committed commit relationships or other deep structures.
     * Use assemblers for complete user graph assembly.
     *
     * @param domain The User domain object to convert
     * @return The UserEntity (structure only, without commit relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toEntity(domain: User): UserEntity {
        // Fast-path: if this User was already mapped in the current context, return it.
        ctx.findEntity<User.Key, User, UserEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        val repositoryEntity = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before UserEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity()."
            )

        val signature = buildString {
            append(domain.name)
            domain.email?.let { append(" <").append(it).append('>') }
        }

        @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
        val entity = UserEntity(
            id = domain.id,
            iid = domain.iid.value,
            gitSignature = signature,
            repository = repositoryEntity
        )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a UserEntity to User domain object.
     *
     * **Precondition**: The User must already exist in MappingContext OR the referenced Repository
     * must be in MappingContext. This is necessary because the User domain model requires a repository.
     *
     * **Note**: This method does NOT map authored/committed commit relationships or other deep structures.
     * Use assemblers for complete user graph assembly.
     *
     * @param entity The UserEntity to convert
     * @return The User domain object from MappingContext or a newly constructed one
     * @throws IllegalStateException if neither User nor Repository is in MappingContext
     */
    override fun toDomain(entity: UserEntity): User {
        // Fast-path: Check if already mapped
        ctx.findDomain<User, UserEntity>(entity)?.let { return it }

        // Try to find Repository in context
        val repository = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before User. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        // Extract name and email from git signature
        val nameRegex = Regex("""^(.+?)\s*<""")
        val emailRegex = Regex("""<([^>]+)>$""")

        val name = nameRegex.find(entity.gitSignature)?.groupValues?.get(1)?.trim()
            ?: entity.gitSignature
        val email = emailRegex.find(entity.gitSignature)?.groupValues?.get(1)

        // Create domain with iid from entity
        val domain = User(
            name = name,
            repository = repository
        ).apply {
            id = entity.id
            this.email = email
        }

        @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)
        return domain
    }

    override fun toDomainList(entities: Iterable<UserEntity>): List<User> = entities.map { toDomain(it) }
}

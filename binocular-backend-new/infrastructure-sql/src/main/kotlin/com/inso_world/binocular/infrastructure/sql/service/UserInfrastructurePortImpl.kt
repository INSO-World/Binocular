package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDeveloperDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import kotlin.uuid.ExperimentalUuidApi

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
@Validated
internal class UserInfrastructurePortImpl(
    @Autowired private val developerDao: IDeveloperDao,
    @Autowired private val repositoryDao: RepositoryDao,
    @Autowired private var repositoryMapper: RepositoryMapper,
    @Autowired private var projectMapper: ProjectMapper,
    @Autowired private var repositoryAssembler: RepositoryAssembler,
) : AbstractInfrastructurePort<User, DeveloperEntity, Long>(Long::class),
    UserInfrastructurePort {
    companion object {
        val logger by logger()
    }

    /**
     * Self-reference to this bean's proxy instance.
     *
     * **Workaround for Spring AOP + Kotlin Value Class Issue**
     *
     * This self-injection is required to work around a limitation where Spring AOP's aspect pointcut
     * matching fails for methods with Kotlin value class parameters (inline classes) that require
     * name mangling via `@JvmName`.
     *
     * **Problem**: When a method like `findByIid(iid: User.Id)` overrides an interface method and
     * uses a value class parameter, Kotlin mangles the JVM method name (e.g., `findByIid-pip`).
     * Spring AOP's `@annotation` pointcut cannot properly match `@MappingSession` on mangled methods,
     * causing the `MappingSessionAspect` to not be triggered.
     *
     * **Solution**: Internal method calls bypass Spring's proxy. By injecting `self` and calling
     * `self.findByIidInternal()`, we ensure the call goes through the Spring AOP proxy, allowing
     * the aspect to intercept and establish the required mapping session scope.
     *
     * @see findByIid
     * @see findByIidInternal
     */
    @Autowired
    @Lazy
    private lateinit var self: UserInfrastructurePortImpl

    @PostConstruct
    fun init() {
        super.dao = developerDao
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): User? {
        logger.trace("Finding user with id: $id")
        return this.developerDao.findById(id.toLong())?.toLegacyUser()
    }

    /**
     * Finds a user by its internal identifier (iid).
     *
     * **Implementation Note - Value Class Workaround**:
     * This method delegates to [findByIidInternal] via [self] (the proxy instance) to ensure
     * Spring AOP aspects are triggered. Direct implementation here would bypass the proxy due to
     * Kotlin's value class name mangling preventing proper aspect pointcut matching.
     *
     * @param iid The user's technical identifier
     * @return The user if found, null otherwise
     * @see self
     * @see findByIidInternal
     */
    override fun findByIid(iid: User.Id): @Valid User? = self.findByIidInternal(iid)

    /**
     * Internal implementation of user lookup by iid.
     *
     * **Why this method exists**:
     * This separate method is required because Spring AOP cannot intercept methods with
     * mangled signatures (caused by Kotlin value class parameters). By extracting
     * the logic here with a normal method name, Spring AOP can properly intercept the call when
     * invoked via [self], establishing the `@MappingSession` scope needed by [repositoryAssembler].
     *
     * **Visibility**: Must not be `private` to allow Spring CGLIB to create
     * a proxy subclass that can override this method for aspect interception.
     *
     * @param iid The user's technical identifier
     * @return The user if found, null otherwise
     * @see findByIid
     * @see MappingSession
     */
    @MappingSession
    @Transactional(readOnly = true)
    protected fun findByIidInternal(iid: User.Id): User? = this.developerDao.findByIid(iid)?.toLegacyUser()

    override fun findCommitsByUserId(userId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findIssuesByUserId(userId: String): List<Issue> {
        TODO("Not yet implemented")
    }

    override fun findFilesByUserId(userId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun update(value: User): User = throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")

    override fun create(value: User): User = throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")

    override fun saveAll(values: Collection<User>): Iterable<User> =
        throw UnsupportedOperationException("User API is deprecated; use Repository aggregate")

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<User> {
        val developers = super<AbstractInfrastructurePort>.findAllEntities()
        return developers.mapNotNull { it.toLegacyUser() }
    }

    @MappingSession
    override fun findAll(repository: Repository): Iterable<User> = emptyList()

    override fun findAll(pageable: Pageable): Page<User> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun DeveloperEntity.toLegacyUser(): User? {
        val repositoryDomain = repositoryAssembler.toDomain(this.repository)
        return User(name = this.name, repository = repositoryDomain).apply {
            this.id = this@toLegacyUser.id?.toString()
            this.email = this@toLegacyUser.email
            setField(
                this.javaClass.superclass.getDeclaredField("iid"),
                this,
                User.Id(this@toLegacyUser.iid.value)
            )
        }
    }
}

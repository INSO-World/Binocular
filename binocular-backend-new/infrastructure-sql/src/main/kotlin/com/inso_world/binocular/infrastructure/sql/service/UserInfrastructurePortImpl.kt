package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

/**
 * Implementation of the UserService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
@Validated
internal class UserInfrastructurePortImpl(
    @Autowired private val userDao: IUserDao,
    @Autowired private val repositoryDao: RepositoryDao,
    @Autowired private var userMapper: UserMapper,
    @Autowired private var commitMapper: CommitMapper,
) : AbstractInfrastructurePort<User, UserEntity, Long>(Long::class),
    UserInfrastructurePort {
    @Autowired
    private lateinit var repositoryAssembler: RepositoryAssembler
    var logger: Logger = LoggerFactory.getLogger(UserInfrastructurePortImpl::class.java)

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @PostConstruct
    fun init() {
        super.dao = userDao
    }

    override fun findById(id: String): User? {
        TODO("Not yet implemented")
    }

    override fun findByIid(iid: User.Id): @Valid User? {
        TODO("Not yet implemented")
    }

    override fun findCommitsByUserId(userId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findIssuesByUserId(userId: String): List<Issue> {
        TODO("Not yet implemented")
    }

    override fun findFilesByUserId(userId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun update(value: User): User {
        TODO("Not yet implemented")
    }


    override fun create(value: User): User {
        TODO("Not yet implemented")
    }

    override fun saveAll(values: Collection<User>): Iterable<User> {
        TODO("Not yet implemented")
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<User> {
        val users = super<AbstractInfrastructurePort>.findAllEntities()

        // Group users by repository to process related users together
        return users
            .groupBy { it.repository }
            .flatMap { (repoEntity, _) ->
                repositoryAssembler.toDomain(repoEntity).user
            }
    }

    @MappingSession
    override fun findAll(repository: Repository): Iterable<User> {
        TODO()
//        val repoEntity =
//            this.repositoryDao.findByName(repository.localPath) ?: throw NotFoundException("Could not find repository $repository")
//
//        this.userDao
//            .findAll(repoEntity)
//            .map {
//                userMapper.toDomain(it)
//            }
    }

    override fun findAll(pageable: Pageable): Page<User> {
        TODO("Not yet implemented")
    }
}

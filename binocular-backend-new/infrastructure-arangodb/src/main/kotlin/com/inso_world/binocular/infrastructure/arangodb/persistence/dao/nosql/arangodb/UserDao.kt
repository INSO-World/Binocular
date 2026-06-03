package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IUserDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.UserMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
internal class UserDao
    @Autowired
    constructor(
        @Autowired private val userRepository: UserRepository,
        @Autowired private val userMapper: UserMapper,
        @Autowired private val seeder: DefaultMappingContextSeeder,
        @Autowired private val repositoryAssembler: RepositoryAssembler
    ) : MappedArangoDbDao<User, UserEntity, String>(userRepository, userMapper),
        IUserDao {
        override fun findAll(): Iterable<User> {
            seeder.seed()
            return userRepository.findAll().map { entity ->
                repositoryAssembler.toDomain(entity.repository)
                userMapper.toDomain(entity)
            }
        }

        override fun findById(id: String): User? {
            seeder.seed()
            val entity = userRepository.findById(id).orElse(null) ?: return null
            repositoryAssembler.toDomain(entity.repository)
            return userMapper.toDomain(entity)
        }

        override fun findAll(pageable: Pageable): Page<User> {
            seeder.seed()
            val result = userRepository.findAll(pageable)
            val content =
                result.content.map { entity ->
                    repositoryAssembler.toDomain(entity.repository)
                    userMapper.toDomain(entity)
                }
            return Page(content, result.totalElements, pageable)
        }
    }

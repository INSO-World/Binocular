package com.inso_world.binocular.web.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitFileUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileUserConnectionDao
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileUserConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitFileUserConnectionDao.
 * 
 * This class adapts the existing CommitFileUserConnectionRepository to work with
 * the new domain model and entity structure.
 */
@Repository
@Profile("nosql", "arangodb")
class CommitFileUserConnectionDao @Autowired constructor(
    private val repository: CommitFileUserConnectionRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val fileMapper: FileMapper,
    private val userMapper: UserMapper
) : ICommitFileUserConnectionDao {

    /**
     * Find all users connected to a file
     */
    override fun findUsersByFile(fileId: String): List<User> {
        val userEntities = repository.findUsersByCommitFile(fileId)
        return userEntities.map { userMapper.toDomain(it) }
    }

    /**
     * Find all files connected to a user
     */
    override fun findFilesByUser(userId: String): List<File> {
        val fileEntities = repository.findCommitFilesByUser(userId)
        return fileEntities.map { fileMapper.toDomain(it) }
    }

    /**
     * Save a commit-file-user connection
     */
    override fun save(connection: CommitFileUserConnection): CommitFileUserConnection {
        // Get the file and user entities from their repositories
        val fileEntity = fileRepository.findById(connection.from.id!!).orElseThrow { 
            IllegalArgumentException("File with ID ${connection.from.id} not found") 
        }
        val userEntity = userRepository.findById(connection.to.id!!).orElseThrow { 
            IllegalArgumentException("User with ID ${connection.to.id} not found") 
        }

        // Convert domain model to the entity format
        val entity = CommitFileUserConnectionEntity(
            id = connection.id,
            from = fileEntity,
            to = userEntity
        )

        // Save using the repository
        val savedEntity = repository.save(entity)

        // Convert back to domain model
        return CommitFileUserConnection(
            id = savedEntity.id,
            from = fileMapper.toDomain(savedEntity.from),
            to = userMapper.toDomain(savedEntity.to)
        )
    }

    /**
     * Delete all commit-file-user connections
     */
    override fun deleteAll() {
        repository.deleteAll()
    }
}

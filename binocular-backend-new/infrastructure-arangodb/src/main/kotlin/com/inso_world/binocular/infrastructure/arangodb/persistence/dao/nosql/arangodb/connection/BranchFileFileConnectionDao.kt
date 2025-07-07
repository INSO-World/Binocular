package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IBranchFileFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.FileMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.BranchFileFileConnectionRepository
import com.inso_world.binocular.model.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IBranchFileFileConnectionDao.
 *
 * This class adapts the existing BranchFileFileConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
class BranchFileFileConnectionDao
    @Autowired
    constructor(
        private val repository: BranchFileFileConnectionRepository,
        private val fileMapper: FileMapper,
    ) : IBranchFileFileConnectionDao {
        /**
         * Find all files connected to a branch file
         */
        override fun findFilesByBranchFile(branchFileId: String): List<File> {
            val fileEntities = repository.findFilesByBranchFile(branchFileId)
            return fileEntities.map { fileMapper.toDomain(it) }
        }

        /**
         * Find all branch files connected to a file
         */
        override fun findBranchFilesByFile(fileId: String): List<File> {
            val fileEntities = repository.findBranchFilesByFile(fileId)
            return fileEntities.map { fileMapper.toDomain(it) }
        }

        /**
         * Delete all branch-file-file connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }

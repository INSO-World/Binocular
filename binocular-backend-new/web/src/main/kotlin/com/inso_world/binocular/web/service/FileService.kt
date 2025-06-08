package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.FileDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FileService(
  @Autowired private val fileDao: FileDao,
  @Autowired private val branchFileConnectionRepository: BranchFileConnectionRepository,
  @Autowired private val branchFileFileConnectionRepository: BranchFileFileConnectionRepository,
  @Autowired private val commitFileConnectionRepository: CommitFileConnectionRepository,
  @Autowired private val commitFileUserConnectionRepository: CommitFileUserConnectionRepository,
  @Autowired private val moduleFileConnectionRepository: ModuleFileConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(FileService::class.java)

  fun findAll(pageable: Pageable): Iterable<File> {
    logger.trace("Getting all files with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return fileDao.findAll(pageable)
  }

  fun findById(id: String): File? {
    logger.trace("Getting file by id: $id")
    return fileDao.findById(id)
  }

  fun findBranchesByFileId(fileId: String): List<Branch> {
    logger.trace("Getting branches for file: $fileId")
    return branchFileConnectionRepository.findBranchesByFile(fileId)
  }

  fun findCommitsByFileId(fileId: String): List<Commit> {
    logger.trace("Getting commits for file: $fileId")
    return commitFileConnectionRepository.findCommitsByFile(fileId)
  }

  fun findModulesByFileId(fileId: String): List<Module> {
    logger.trace("Getting modules for file: $fileId")
    return moduleFileConnectionRepository.findModulesByFile(fileId)
  }

  fun findRelatedFilesByFileId(fileId: String): List<File> {
    logger.trace("Getting related files for file: $fileId")
    return branchFileFileConnectionRepository.findFilesByBranchFile(fileId)
  }

  fun findUsersByFileId(fileId: String): List<User> {
    logger.trace("Getting users for file: $fileId")
    return commitFileUserConnectionRepository.findUsersByCommitFile(fileId)
  }
}

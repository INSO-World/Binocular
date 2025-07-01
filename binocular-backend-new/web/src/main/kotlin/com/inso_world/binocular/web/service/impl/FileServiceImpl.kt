package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleFileConnectionDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import com.inso_world.binocular.web.service.FileService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FileServiceImpl(
  @Autowired private val fileDao: IFileDao,
  @Autowired private val branchFileConnectionRepository: IBranchFileConnectionDao,
  @Autowired private val branchFileFileConnectionRepository: IBranchFileFileConnectionDao,
  @Autowired private val commitFileConnectionRepository: ICommitFileConnectionDao,
  @Autowired private val commitFileUserConnectionRepository: ICommitFileUserConnectionDao,
  @Autowired private val moduleFileConnectionRepository: IModuleFileConnectionDao
) : FileService {

  var logger: Logger = LoggerFactory.getLogger(FileServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<File> {
    logger.trace("Getting all files with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return fileDao.findAll(pageable)
  }

  override fun findById(id: String): File? {
    logger.trace("Getting file by id: $id")
    return fileDao.findById(id)
  }

  override fun findBranchesByFileId(fileId: String): List<Branch> {
    logger.trace("Getting branches for file: $fileId")
    return branchFileConnectionRepository.findBranchesByFile(fileId)
  }

  override fun findCommitsByFileId(fileId: String): List<Commit> {
    logger.trace("Getting commits for file: $fileId")
    return commitFileConnectionRepository.findCommitsByFile(fileId)
  }

  override fun findModulesByFileId(fileId: String): List<Module> {
    logger.trace("Getting modules for file: $fileId")
    return moduleFileConnectionRepository.findModulesByFile(fileId)
  }

  override fun findRelatedFilesByFileId(fileId: String): List<File> {
    logger.trace("Getting related files for file: $fileId")
    return branchFileFileConnectionRepository.findFilesByBranchFile(fileId)
  }

  override fun findUsersByFileId(fileId: String): List<User> {
    logger.trace("Getting users for file: $fileId")
    return commitFileUserConnectionRepository.findUsersByFile(fileId)
  }
}

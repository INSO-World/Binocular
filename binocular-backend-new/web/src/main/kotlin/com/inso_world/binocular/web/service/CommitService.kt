package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommitService(
  @Autowired private val commitDao: CommitDao,
  @Autowired private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
  @Autowired private val commitFileConnectionRepository: CommitFileConnectionRepository,
  @Autowired private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
  @Autowired private val commitUserConnectionRepository: CommitUserConnectionRepository,
  @Autowired private val issueCommitConnectionRepository: IssueCommitConnectionRepository,
  @Autowired private val buildService: BuildService,
  @Autowired private val fileService: FileService,
  @Autowired private val moduleService: ModuleService,
  @Autowired private val userService: UserService,
  @Autowired private val issueService: IssueService,
) {

  var logger: Logger = LoggerFactory.getLogger(CommitService::class.java)


  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Commit> {
    logger.trace("Getting all commits...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page - 1, perPage)

    return commitDao.findAll(pageable)
  }

  fun findById(id: String): Commit? {
    logger.trace("Getting commit by id: $id")
    return commitDao.findById(id)
  }

  fun findBuildsByCommitId(commitId: String): List<Build> {
    logger.trace("Getting builds for commit: $commitId")
    return commitBuildConnectionRepository.findAll()
      .filter { it.from == "commits/$commitId" }
      .mapNotNull { it.to?.removePrefix("builds/") }
      .mapNotNull { buildService.findById(it) }
  }

  fun findFilesByCommitId(commitId: String): List<File> {
    logger.trace("Getting files for commit: $commitId")
    return commitFileConnectionRepository.findAll()
      .filter { it.from == "commits/$commitId" }
      .mapNotNull { it.to?.removePrefix("files/") }
      .mapNotNull { fileService.findById(it) }
  }

  fun findModulesByCommitId(commitId: String): List<Module> {
    logger.trace("Getting modules for commit: $commitId")
    return commitModuleConnectionRepository.findAll()
      .filter { it.from == "commits/$commitId" }
      .mapNotNull { it.to?.removePrefix("modules/") }
      .mapNotNull { moduleService.findById(it) }
  }

  fun findUsersByCommitId(commitId: String): List<User> {
    logger.trace("Getting users for commit: $commitId")
    return commitUserConnectionRepository.findAll()
      .filter { it.from == "commits/$commitId" }
      .mapNotNull { it.to?.removePrefix("users/") }
      .mapNotNull { userService.findById(it) }
  }

  fun findIssuesByCommitId(commitId: String): List<Issue> {
    logger.trace("Getting issues for commit: $commitId")
    return issueCommitConnectionRepository.findAll()
      .filter { it.to == "commits/$commitId" }
      .mapNotNull { it.from?.removePrefix("issues/") }
      .mapNotNull { issueService.findById(it) }
  }

}

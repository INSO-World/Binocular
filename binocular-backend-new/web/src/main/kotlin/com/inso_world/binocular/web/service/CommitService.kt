package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitCommitConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommitService(
  @Autowired private val commitDao: CommitDao,
  @Autowired private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
  @Autowired private val commitCommitConnectionRepository: CommitCommitConnectionRepository,
  @Autowired private val commitFileConnectionRepository: CommitFileConnectionRepository,
  @Autowired private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
  @Autowired private val commitUserConnectionRepository: CommitUserConnectionRepository,
  @Autowired private val issueCommitConnectionRepository: IssueCommitConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(CommitService::class.java)


  fun findAll(pageable: Pageable): Iterable<Commit> {
    logger.trace("Getting all commits with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return commitDao.findAll(pageable)
  }

  fun findById(id: String): Commit? {
    logger.trace("Getting commit by id: $id")
    return commitDao.findById(id)
  }

  fun findBuildsByCommitId(commitId: String): List<Build> {
    logger.trace("Getting builds for commit: $commitId")
    return commitBuildConnectionRepository.findBuildsByCommit(commitId)
  }

  fun findFilesByCommitId(commitId: String): List<File> {
    logger.trace("Getting files for commit: $commitId")
    return commitFileConnectionRepository.findFilesByCommit(commitId)
  }

  fun findModulesByCommitId(commitId: String): List<Module> {
    logger.trace("Getting modules for commit: $commitId")
    return commitModuleConnectionRepository.findModulesByCommit(commitId)
  }

  fun findUsersByCommitId(commitId: String): List<User> {
    logger.trace("Getting users for commit: $commitId")
    return commitUserConnectionRepository.findUsersByCommit(commitId)
  }

  fun findIssuesByCommitId(commitId: String): List<Issue> {
    logger.trace("Getting issues for commit: $commitId")
    return issueCommitConnectionRepository.findIssuesByCommit(commitId)
  }

  fun findParentCommitsByChildCommitId(childCommitId: String): List<Commit> {
    logger.trace("Getting parent commits for child commit: $childCommitId")
    return commitCommitConnectionRepository.findParentCommitsByChildCommit(childCommitId)
  }

  fun findChildCommitsByParentCommitId(parentCommitId: String): List<Commit> {
    logger.trace("Getting child commits for parent commit: $parentCommitId")
    return commitCommitConnectionRepository.findChildCommitsByParentCommit(parentCommitId)
  }

}

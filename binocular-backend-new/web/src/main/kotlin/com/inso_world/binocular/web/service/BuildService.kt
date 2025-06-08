package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BuildDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BuildService(
  @Autowired private val buildDao: BuildDao,
  @Autowired private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
) {

  var logger: Logger = LoggerFactory.getLogger(BuildService::class.java)

  fun findAll(pageable: Pageable): Iterable<Build> {
    logger.trace("Getting all builds with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return buildDao.findAll(pageable)
  }

  fun findById(id: String): Build? {
    logger.trace("Getting build by id: $id")
    return buildDao.findById(id)
  }

  fun findCommitsByBuildId(buildId: String): List<Commit> {
    logger.trace("Getting commits for build: $buildId")
    return commitBuildConnectionRepository.findCommitsByBuild(buildId)
  }
}

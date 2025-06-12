package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.BuildDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import com.inso_world.binocular.web.service.BuildService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BuildServiceImpl(
  @Autowired private val buildDao: BuildDao,
  @Autowired private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
) : BuildService {

  var logger: Logger = LoggerFactory.getLogger(BuildServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<Build> {
    logger.trace("Getting all builds with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return buildDao.findAll(pageable)
  }

  override fun findAll(pageable: Pageable, until: Long?): Page<Build> {
    logger.trace("Getting builds with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}, until=$until")

    if (until == null) {
      return findAll(pageable)
    }

    val allBuilds = buildDao.findAll(pageable)
    val filteredBuilds = allBuilds.content.filter { build ->
      build.committedAt?.time?.let { committedTime ->
        committedTime <= until
      } ?: true // Include builds with null committedAt
    }

    return Page(filteredBuilds, filteredBuilds.size.toLong(), pageable)
  }

  override fun findById(id: String): Build? {
    logger.trace("Getting build by id: $id")
    return buildDao.findById(id)
  }

  override fun findCommitsByBuildId(buildId: String): List<Commit> {
    logger.trace("Getting commits for build: $buildId")
    return commitBuildConnectionRepository.findCommitsByBuild(buildId)
  }
}

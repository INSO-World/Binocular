package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BuildInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBuildDao
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.Date

@Service
class BuildInfrastructurePortImpl : BuildInfrastructurePort {
    @Autowired private lateinit var buildDao: IBuildDao

    @Autowired private lateinit var commitBuildConnectionRepository: ICommitBuildConnectionDao
    var logger: Logger = LoggerFactory.getLogger(BuildInfrastructurePortImpl::class.java)

    override fun findAll(pageable: Pageable): Page<Build> {
        logger.trace("Getting all builds with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return buildDao.findAll(pageable)
    }

    override fun findAll(
        pageable: Pageable,
        since: Long?,
        until: Long?,
    ): Page<Build> {
        logger.trace("Getting builds with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}, since=$since, until=$until")
        return buildDao.findAll(pageable, since, until)
    }

    override fun findById(id: String): Build? {
        logger.trace("Getting build by id: $id")
        return buildDao.findById(id)
    }

    override fun findCommitsByBuildId(buildId: String): List<Commit> {
        logger.trace("Getting commits for build: $buildId")
        return commitBuildConnectionRepository.findCommitsByBuild(buildId)
    }

    override fun findAll(): Iterable<Build> = this.buildDao.findAll()

    override fun create(entity: Build): Build = this.buildDao.save(entity)

    override fun saveAll(entities: Collection<Build>): Iterable<Build> = this.buildDao.saveAll(entities)

    override fun delete(entity: Build) = this.buildDao.delete(entity)

    override fun update(entity: Build): Build {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(entity: Build): Build {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        this.buildDao.deleteAll()
    }
}

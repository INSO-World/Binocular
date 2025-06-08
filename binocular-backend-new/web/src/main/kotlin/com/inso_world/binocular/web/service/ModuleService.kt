package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.ModuleDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleModuleConnectionRepository
import com.inso_world.binocular.web.service.interfaces.ModuleServiceInterface
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ModuleService(
  @Autowired private val moduleDao: ModuleDao,
  @Autowired private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
  @Autowired private val moduleFileConnectionRepository: ModuleFileConnectionRepository,
  @Autowired private val moduleModuleConnectionRepository: ModuleModuleConnectionRepository
) : ModuleServiceInterface {

  var logger: Logger = LoggerFactory.getLogger(ModuleService::class.java)

  override fun findAll(pageable: Pageable): Iterable<Module> {
    logger.trace("Getting all modules with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return moduleDao.findAll(pageable)
  }

  override fun findById(id: String): Module? {
    logger.trace("Getting module by id: $id")
    return moduleDao.findById(id)
  }

  override fun findCommitsByModuleId(moduleId: String): List<Commit> {
    logger.trace("Getting commits for module: $moduleId")
    return commitModuleConnectionRepository.findCommitsByModule(moduleId)
  }

  override fun findFilesByModuleId(moduleId: String): List<File> {
    logger.trace("Getting files for module: $moduleId")
    return moduleFileConnectionRepository.findFilesByModule(moduleId)
  }

  override fun findChildModulesByModuleId(moduleId: String): List<Module> {
    logger.trace("Getting child modules for module: $moduleId")
    return moduleModuleConnectionRepository.findChildModulesByModule(moduleId)
  }

  override fun findParentModulesByModuleId(moduleId: String): List<Module> {
    logger.trace("Getting parent modules for module: $moduleId")
    return moduleModuleConnectionRepository.findParentModulesByModule(moduleId)
  }
}

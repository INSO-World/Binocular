package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.FileDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FileService(
  @Autowired private val fileDao: FileDao,
) {

  var logger: Logger = LoggerFactory.getLogger(FileService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<File> {
    logger.trace("Getting all files...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page - 1, perPage)

    return fileDao.findAll(pageable)
  }

  fun findById(id: String): File? {
    logger.trace("Getting file by id: $id")
    return fileDao.findById(id)
  }
}

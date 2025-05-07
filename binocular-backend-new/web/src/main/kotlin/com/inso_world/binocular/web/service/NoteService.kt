package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.NoteDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class NoteService(
  @Autowired private val noteDao: NoteDao,
) {

  var logger: Logger = LoggerFactory.getLogger(NoteService::class.java)

  fun findAll(page: Int? = 1, perPage: Int? = 100): Iterable<Note> {
    logger.trace("Getting all notes...")
    val page = page ?: 1
    val perPage = perPage ?: 100
    logger.debug("page is $page, perPage is $perPage")
    val pageable: Pageable = PageRequest.of(page, perPage)

    return noteDao.findAll()
  }

  fun findById(id: String): Note? {
    logger.trace("Getting note by id: $id")
    return noteDao.findById(id)
  }
}

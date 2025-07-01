package com.inso_world.binocular.web.service.impl

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.model.Page
import com.inso_world.binocular.web.service.NoteService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Implementation of the NoteService interface.
 * This service is database-agnostic and works with both ArangoDB and SQL implementations.
 */
@Service
class NoteServiceImpl(
  @Autowired private val noteDao: INoteDao,
  @Autowired private val noteAccountConnectionRepository: INoteAccountConnectionDao,
  @Autowired private val issueNoteConnectionRepository: IIssueNoteConnectionDao,
  @Autowired private val mergeRequestNoteConnectionRepository: IMergeRequestNoteConnectionDao
) : NoteService {

  var logger: Logger = LoggerFactory.getLogger(NoteServiceImpl::class.java)

  override fun findAll(pageable: Pageable): Page<Note> {
    logger.trace("Getting all notes with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
    return noteDao.findAll(pageable)
  }

  override fun findById(id: String): Note? {
    logger.trace("Getting note by id: $id")
    return noteDao.findById(id)
  }

  override fun findAccountsByNoteId(noteId: String): List<Account> {
    logger.trace("Getting accounts for note: $noteId")
    return noteAccountConnectionRepository.findAccountsByNote(noteId)
  }

  override fun findIssuesByNoteId(noteId: String): List<Issue> {
    logger.trace("Getting issues for note: $noteId")
    return issueNoteConnectionRepository.findIssuesByNote(noteId)
  }

  override fun findMergeRequestsByNoteId(noteId: String): List<MergeRequest> {
    logger.trace("Getting merge requests for note: $noteId")
    return mergeRequestNoteConnectionRepository.findMergeRequestsByNote(noteId)
  }
}

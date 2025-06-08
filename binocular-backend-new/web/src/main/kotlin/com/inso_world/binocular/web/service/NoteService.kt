package com.inso_world.binocular.web.service

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.nosql.arangodb.NoteDao
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestNoteConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.NoteAccountConnectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class NoteService(
  @Autowired private val noteDao: NoteDao,
  @Autowired private val noteAccountConnectionRepository: NoteAccountConnectionRepository,
  @Autowired private val issueNoteConnectionRepository: IssueNoteConnectionRepository,
  @Autowired private val mergeRequestNoteConnectionRepository: MergeRequestNoteConnectionRepository
) {

  var logger: Logger = LoggerFactory.getLogger(NoteService::class.java)

  fun findAll(pageable: Pageable): Iterable<Note> {
    logger.trace("Getting all notes with pageable: page=${pageable.pageNumber + 1}, size=${pageable.pageSize}")
    return noteDao.findAll(pageable)
  }

  fun findById(id: String): Note? {
    logger.trace("Getting note by id: $id")
    return noteDao.findById(id)
  }

  fun findAccountsByNoteId(noteId: String): List<Account> {
    logger.trace("Getting accounts for note: $noteId")
    return noteAccountConnectionRepository.findAccountsByNote(noteId)
  }

  fun findIssuesByNoteId(noteId: String): List<Issue> {
    logger.trace("Getting issues for note: $noteId")
    return issueNoteConnectionRepository.findIssuesByNote(noteId)
  }

  fun findMergeRequestsByNoteId(noteId: String): List<MergeRequest> {
    logger.trace("Getting merge requests for note: $noteId")
    return mergeRequestNoteConnectionRepository.findMergeRequestsByNote(noteId)
  }
}

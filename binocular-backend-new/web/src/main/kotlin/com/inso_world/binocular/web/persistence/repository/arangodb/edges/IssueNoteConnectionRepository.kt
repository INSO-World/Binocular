package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.edge.IssueNoteConnection
import org.springframework.stereotype.Repository

@Repository
interface IssueNoteConnectionRepository: ArangoRepository<IssueNoteConnection, String> {

  @Query("""
    FOR c IN `issues-notes`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR n IN notes
            FILTER n._id == c._to
            RETURN n
""")
  fun findNotesByIssue(issueId: String): List<Note>

  @Query("""
    FOR c IN `issues-notes`
        FILTER c._to == CONCAT('notes/', @noteId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByNote(noteId: String): List<Issue>
}

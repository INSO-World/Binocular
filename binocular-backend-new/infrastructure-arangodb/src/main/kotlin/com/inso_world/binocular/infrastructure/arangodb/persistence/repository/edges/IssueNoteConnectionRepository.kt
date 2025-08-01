package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueNoteConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface IssueNoteConnectionRepository : ArangoRepository<IssueNoteConnectionEntity, String> {
    @Query(
        """
    FOR c IN `issues-notes`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR n IN notes
            FILTER n._id == c._to
            RETURN n
""",
    )
    fun findNotesByIssue(issueId: String): List<NoteEntity>

    @Query(
        """
    FOR c IN `issues-notes`
        FILTER c._to == CONCAT('notes/', @noteId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""",
    )
    fun findIssuesByNote(noteId: String): List<IssueEntity>
}

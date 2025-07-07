package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestNoteConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface MergeRequestNoteConnectionRepository : ArangoRepository<MergeRequestNoteConnectionEntity, String> {
    @Query(
        """
    FOR c IN `merge-requests-notes`
        FILTER c._from == CONCAT('mergeRequests/', @mergeRequestId)
        FOR n IN notes
            FILTER n._id == c._to
            RETURN n
""",
    )
    fun findNotesByMergeRequest(mergeRequestId: String): List<NoteEntity>

    @Query(
        """
    FOR c IN `merge-requests-notes`
        FILTER c._to == CONCAT('notes/', @noteId)
        FOR mr IN mergeRequests
            FILTER mr._id == c._from
            RETURN mr
""",
    )
    fun findMergeRequestsByNote(noteId: String): List<MergeRequestEntity>
}

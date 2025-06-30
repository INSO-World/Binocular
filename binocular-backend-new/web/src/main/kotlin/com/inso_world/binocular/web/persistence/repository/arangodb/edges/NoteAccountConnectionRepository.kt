package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.NoteAccountConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.NoteEntity
import org.springframework.stereotype.Repository

@Repository
interface NoteAccountConnectionRepository: ArangoRepository<NoteAccountConnectionEntity, String> {

  @Query("""
    FOR c IN `notes-accounts`
        FILTER c._from == CONCAT('notes/', @noteId)
        FOR a IN accounts
            FILTER a._id == c._to
            RETURN a
""")
  fun findAccountsByNote(noteId: String): List<AccountEntity>

  @Query("""
    FOR c IN `notes-accounts`
        FILTER c._to == CONCAT('accounts/', @accountId)
        FOR n IN notes
            FILTER n._id == c._from
            RETURN n
""")
  fun findNotesByAccount(accountId: String): List<NoteEntity>
}

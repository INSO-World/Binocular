package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Note
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository: ArangoRepository<Note, String> {
}

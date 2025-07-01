package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.entity.arangodb.NoteEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("nosql", "arangodb")
interface NoteRepository: ArangoRepository<NoteEntity, String> {
}

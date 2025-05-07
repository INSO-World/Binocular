package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.repository.arangodb.NoteRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class NoteDao(
  @Autowired private val noteRepository: NoteRepository
) : ArangoDbDao<Note, String>(), INoteDao {

  init {
    this.setClazz(Note::class.java)
    this.setRepository(noteRepository)
  }
}

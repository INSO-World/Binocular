package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.arangodb.NoteEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.NoteMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.NoteRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("nosql", "arangodb")
class NoteDao @Autowired constructor(
  noteRepository: NoteRepository,
  noteMapper: NoteMapper
) : MappedArangoDbDao<Note, NoteEntity, String>(noteRepository, noteMapper), INoteDao

package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.INoteDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.NoteMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.NoteRepository
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class NoteDao
    @Autowired
    constructor(
        noteRepository: NoteRepository,
        noteMapper: NoteMapper,
    ) : MappedArangoDbDao<Note, NoteEntity, String>(noteRepository, noteMapper),
        INoteDao

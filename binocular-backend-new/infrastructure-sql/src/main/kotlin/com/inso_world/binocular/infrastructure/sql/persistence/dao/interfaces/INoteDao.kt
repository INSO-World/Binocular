package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.NoteEntity

internal interface INoteDao : IDao<NoteEntity, Long> {
    fun findByIid(iid: com.inso_world.binocular.model.Note.Id): NoteEntity?
}

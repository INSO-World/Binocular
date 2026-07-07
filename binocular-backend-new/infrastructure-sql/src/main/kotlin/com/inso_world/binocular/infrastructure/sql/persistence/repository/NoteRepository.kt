package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.NoteEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
internal interface NoteRepository : JpaRepository<NoteEntity, Long> {
    @OptIn(ExperimentalUuidApi::class)
    fun findByIid(iid: Uuid): NoteEntity?
}

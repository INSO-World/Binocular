package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.FileEntity
import com.inso_world.binocular.model.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
internal interface FileRepository :
    JpaRepository<FileEntity, Long>,
    JpaSpecificationExecutor<FileEntity> {

    @Query("SELECT f FROM FileEntity f")
//    @EntityGraph("Commit.full")
    fun findAllAsStream(): Stream<FileEntity>

    @OptIn(ExperimentalUuidApi::class)
    fun findByIid(iid: Uuid): FileEntity?

}

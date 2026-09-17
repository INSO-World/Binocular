package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import java.util.stream.Stream

internal interface IFileDao : IDao<FileEntity, Long> {
    fun findByIid(iid: com.inso_world.binocular.model.File.Id): FileEntity?

    fun findAll(repository: Repository): Stream<FileEntity>
}

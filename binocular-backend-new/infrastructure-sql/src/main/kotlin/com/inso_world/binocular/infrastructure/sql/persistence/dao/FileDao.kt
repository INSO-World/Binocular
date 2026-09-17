package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.FileRepository
import jakarta.persistence.criteria.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
* SQL implementation of IFileDao.
*/
@Repository
internal class FileDao(
    @field:Autowired
    private val repo: FileRepository,
) : SqlDao<FileEntity, Long>(),
    IFileDao { 
    init {
        this.setClazz(FileEntity::class.java)
        this.setRepository(repo)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(iid: com.inso_world.binocular.model.File.Id): FileEntity? = repo.findByIid(iid.value)
    
    override fun findAll(repository: com.inso_world.binocular.model.Repository): Stream<FileEntity> {
        return this.repo.findAllAsStream()
    }
    
    override fun findAllAsStream(): Stream<FileEntity> = repo.findAllAsStream()
}